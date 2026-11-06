package com.zian.backend.borrow.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.domain.model.BorrowOrder;
import com.zian.backend.borrow.domain.model.ReturnCondition;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import com.zian.backend.borrow.interfaces.dto.BorrowDtos;
import com.zian.backend.catalog.application.CatalogService;
import com.zian.backend.catalog.domain.model.Book;
import com.zian.backend.catalog.domain.model.BookCopy;
import com.zian.backend.iam.application.UserQueryService;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.reservation.application.ReservationService;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 借还台应用服务：借出与归还是双聚合（借阅单 + 副本）同事务操作。
 */
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowOrderRepository borrowOrderRepository;
    private final CatalogService catalogService;
    private final UserQueryService userQueryService;
    private final ReservationService reservationService;
    private final RuleService ruleService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;

    @Transactional
    public BorrowDtos.BorrowResult borrow(BorrowDtos.BorrowRequest request) {
        User reader = userQueryService.requireByUserNo(request.readerNo().trim());
        LoginUser actor = UserContext.require();
        if (!actor.isStaff() && !actor.id().equals(reader.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "读者只能为自己借书");
        }        reader.assertCanBorrow("借阅");
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
        Long orderId = borrowOrderRepository.insert(order);

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

    @Transactional
    public BorrowDtos.ReturnResult returnBook(BorrowDtos.ReturnRequest request) {
        BookCopy copy = catalogService.requireCopyByBarcode(request.barcode());
        BorrowOrder order = borrowOrderRepository
                .findActiveByCopyId(copy.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该副本没有在借记录"));
        LoginUser actor = UserContext.require();
        if (!actor.isStaff() && !actor.id().equals(order.getReaderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "读者只能归还自己的借阅");
        }        User reader = userQueryService.requireById(order.getReaderId());
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
     * 宽限批准后把有效应还日期后移（供 grace 上下文调用，跨上下文只走 application 接口）。
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

