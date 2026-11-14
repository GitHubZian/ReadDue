package com.zian.backend.borrow.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.domain.model.BorrowOrder;
import com.zian.backend.borrow.domain.model.BorrowStatus;
import com.zian.backend.borrow.domain.model.ReturnCondition;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import com.zian.backend.borrow.domain.repository.RenewRecordRepository;
import com.zian.backend.borrow.interfaces.dto.BorrowDtos;
import com.zian.backend.catalog.application.CatalogService;
import com.zian.backend.catalog.domain.model.Book;
import com.zian.backend.catalog.domain.model.BookCopy;
import com.zian.backend.iam.application.UserQueryService;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.reservation.application.ReservationService;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.dto.BookRefView;
import com.zian.backend.shared.dto.PersonView;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 借还台应用服务：借出与归还是双聚合（借阅单 + 副本）同事务操作。
 */
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowOrderRepository borrowOrderRepository;
    private final RenewRecordRepository renewRecordRepository;
    private final CatalogService catalogService;
    private final UserQueryService userQueryService;
    private final ReservationService reservationService;
    private final RuleService ruleService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;

    /**
     * 办理借出：校验读者状态、副本可借性与重复借阅，写入借阅单与 outbox 事件。
     *
     * @param request 读者学号与副本条码
     * @return 借阅结果
     */
    @Transactional
    public BorrowDtos.BorrowResult borrow(BorrowDtos.BorrowRequest request) {
        User reader = userQueryService.requireByUserNo(request.readerNo().trim());
        LoginUser actor = UserContext.require();
        if (!actor.isStaff() && !actor.id().equals(reader.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "读者只能为自己借书");
        }
        reader.assertCanBorrow("借阅");
        BookCopy copy = catalogService.requireCopyByBarcode(request.barcode());
        if (!copy.isLendable()) {
            throw new BusinessException(
                    ErrorCode.RULE_VIOLATION, "该副本当前状态为「" + copy.getStatus() + "」，不可借出");
        }
        if (borrowOrderRepository.findActiveByReaderAndBook(reader.getId(), copy.getBookId()).isPresent()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该读者已借阅同一书目，请先归还");
        }

        int loanDays = ruleService.intValue("RENEW.LOAN_DAYS", 30);
        LocalDateTime now = LocalDateTime.now();
        BorrowOrder order =
                BorrowOrder.open(nextOrderNo(), reader.getId(), copy.getBookId(), copy.getId(), now, loanDays);

        catalogService.lend(copy.getId());
        Long orderId;
        try {
            orderId = borrowOrderRepository.insert(order);
        } catch (DuplicateKeyException e) {
            // 并发兜底：uk_borrow_active_copy / uk_borrow_active_reader_book / uk_reservation_active_reader_book
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "该副本刚被借出或该读者已在借同名书目，请刷新后重试");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId);
        payload.put("readerId", reader.getId());
        payload.put("bookId", copy.getBookId());
        payload.put("copyId", copy.getId());
        payload.put("dueAt", String.valueOf(order.getEffectiveDueAt()));
        outboxAppender.append("BORROW_ORDER", orderId, MqConstant.EVENT_BORROW_CREATED, payload);
        auditRecorder.record("BORROW_CREATE", "BORROW_ORDER", orderId, "借出", payload);

        Book book = catalogService.requireBook(copy.getBookId());
        return new BorrowDtos.BorrowResult(
                orderId,
                person(reader),
                bookRef(book, copy.getBarcode()),
                order.getEffectiveDueAt().toString());
    }

    /**
     * 办理归还：结单、恢复副本、触发预约兑现、计算罚金并写入 outbox。
     *
     * @param request 副本条码
     * @return 归还结果（罚金与逾期天数）
     */
    @Transactional
    public BorrowDtos.ReturnResult returnBook(BorrowDtos.ReturnRequest request) {
        BookCopy copy = catalogService.requireCopyByBarcode(request.barcode());
        BorrowOrder order = borrowOrderRepository
                .findActiveByCopyId(copy.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该副本没有在借记录"));
        LoginUser actor = UserContext.require();
        if (!actor.isStaff() && !actor.id().equals(order.getReaderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "读者只能归还自己的借阅");
        }
        User reader = userQueryService.requireById(order.getReaderId());
        LocalDateTime now = LocalDateTime.now();
        int overdueDays = order.returnOn(now, LocalDate.now(), ReturnCondition.GOOD);
        if (borrowOrderRepository.update(order) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "借阅单已被修改，请刷新后重试");
        }
        catalogService.receive(copy.getId(), false);

        // 归还后立即兑现预约队首（READY + 48h 保留）
        reservationService.promoteNext(order.getBookId());

        BigDecimal fine = BigDecimal.valueOf(overdueDays)
                .multiply(ruleService.decimal("FINE.PER_DAY", "0.50"))
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", order.getId());
        payload.put("bookId", order.getBookId());
        payload.put("copyId", copy.getId());
        payload.put("readerId", order.getReaderId());
        payload.put("overdueDays", overdueDays);
        outboxAppender.append("BORROW_ORDER", order.getId(), MqConstant.EVENT_BORROW_RETURNED, payload);
        auditRecorder.record("BORROW_RETURN", "BORROW_ORDER", order.getId(), "归还", payload);

        Book book = catalogService.requireBook(order.getBookId());
        return new BorrowDtos.ReturnResult(
                bookRef(book, copy.getBarcode()), person(reader), fine.toPlainString(), overdueDays);
    }

    /**
     * 续借：校验逾期、续借次数与预约队列，顺延应还日并记录续借流水。
     *
     * @param orderId 借阅单 ID
     * @return 续借结果
     */
    @Transactional
    public BorrowDtos.RenewResult renew(Long orderId) {
        BorrowOrder order = borrowOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "借阅单不存在"));
        LoginUser actor = UserContext.require();
        if (!actor.isStaff() && !actor.id().equals(order.getReaderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "读者只能续借自己的借阅");
        }
        User reader = userQueryService.requireById(order.getReaderId());
        reader.assertCanBorrow("续借");

        int maxRenew = ruleService.intValue("RENEW.MAX_TIMES", 1);
        int periodDays = ruleService.intValue("RENEW.PERIOD_DAYS", 15);
        int queueBlock = ruleService.intValue("RENEW.QUEUE_BLOCK", 4);
        int queueLen = reservationService.countWaiting(order.getBookId());

        String rejectCode = null;
        if (!order.isActive()) {
            rejectCode = "HOLDING";
        } else if (order.getStatus() != BorrowStatus.BORROWED) {
            rejectCode = "OVERDUE";
        } else if (order.getRenewCount() >= maxRenew) {
            rejectCode = "RENEW_LIMIT";
        } else if (queueLen >= queueBlock) {
            rejectCode = "QUEUE_LIMIT";
        }

        String operatorType = actor.isStaff() ? "LIBRARIAN" : "READER";
        LocalDate oldDue = order.getEffectiveDueAt();
        if (rejectCode != null) {
            renewRecordRepository.append(
                    orderId, order.getRenewCount() + 1, oldDue, null, "REJECTED", rejectCode, queueLen,
                    operatorType, actor.id());
            String message = switch (rejectCode) {
                case "HOLDING" -> "该借阅单已结束，无法续借";
                case "OVERDUE" -> "逾期未还不可续借，请先归还";
                case "RENEW_LIMIT" -> "已达最大续借次数（" + maxRenew + " 次）";
                default -> "该书目预约排队已达 " + queueLen + " 人，暂不可续借";
            };
            throw new BusinessException(ErrorCode.RULE_VIOLATION, message);
        }

        order.renew(maxRenew, queueLen, queueBlock, periodDays);
        if (borrowOrderRepository.update(order) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "借阅单已被修改，请刷新后重试");
        }
        renewRecordRepository.append(
                orderId, order.getRenewCount(), oldDue, order.getEffectiveDueAt(), "APPROVED", null, queueLen,
                operatorType, actor.id());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId);
        payload.put("readerId", order.getReaderId());
        payload.put("bookId", order.getBookId());
        payload.put("dueAt", String.valueOf(order.getEffectiveDueAt()));
        payload.put("renewCount", order.getRenewCount());
        outboxAppender.append("BORROW_ORDER", orderId, MqConstant.EVENT_BORROW_RENEWED, payload);
        auditRecorder.record("BORROW_RENEW", "BORROW_ORDER", orderId, "续借", payload);

        return new BorrowDtos.RenewResult(
                orderId,
                order.getDueAt().toString(),
                order.getEffectiveDueAt().toString(),
                order.getRenewCount(),
                maxRenew);
    }

    /**
     * 分页查询当前读者的借阅记录。
     *
     * @param status    状态过滤，空表示全部
     * @param pageQuery 分页参数
     * @return 借阅记录分页
     */
    @Transactional(readOnly = true)
    public PageResult<BorrowDtos.BorrowItem> myBorrows(String status, PageQuery pageQuery) {
        Long readerId = UserContext.requireId();
        List<BorrowOrder> orders =
                borrowOrderRepository.findByReader(readerId, status, (int) pageQuery.offset(), pageQuery.size());
        long total = borrowOrderRepository.countByReader(readerId, status);
        Map<Long, Book> books = catalogService.booksByIds(orders.stream().map(BorrowOrder::getBookId).toList());
        LocalDate today = LocalDate.now();
        List<BorrowDtos.BorrowItem> items = orders.stream()
                .map(order -> {
                    Book book = books.get(order.getBookId());
                    return new BorrowDtos.BorrowItem(
                            order.getId(),
                            order.getOrderNo(),
                            book == null ? null : book.title(),
                            book == null ? null : book.author(),
                            book == null ? null : book.coverUrl(),
                            Times.iso(order.getBorrowedAt()),
                            order.getDueAt(),
                            order.getEffectiveDueAt(),
                            order.getStatus().name(),
                            order.getRenewCount(),
                            order.getGraceDays(),
                            order.overdueDays(today));
                })
                .toList();
        return PageResult.of(items, total, pageQuery);
    }

    /**
     * 读取借阅单，供其他上下文做只读校验。
     *
     * @param orderId 借阅单 ID
     * @return 借阅聚合
     */
    @Transactional(readOnly = true)
    public BorrowOrder requireOrder(Long orderId) {
        return borrowOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "借阅单不存在"));
    }

    /**
     * 应用宽限天数：累加 graceDays 并重算有效应还日期。
     *
     * @param orderId 借阅单 ID
     * @param days    批准天数
     * @return 更新后的借阅聚合
     */
    @Transactional
    public BorrowOrder applyGrace(Long orderId, int days) {
        BorrowOrder order = borrowOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "借阅单不存在"));
        order.applyGrace(days);
        if (borrowOrderRepository.update(order) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "借阅单已被修改，请刷新后重试");
        }
        return order;
    }

    private PersonView person(User user) {
        return new PersonView(user.getId(), user.getName(), user.getUserNo(), null);
    }

    private BookRefView bookRef(Book book, String barcode) {
        return new BookRefView(book.id(), book.title(), book.author(), barcode, null);
    }

    private String nextOrderNo() {
        return "B" + LocalDate.now().toString().replace("-", "") + String.format("%06d",
                ThreadLocalRandom.current().nextInt(1_000_000));
    }
}

