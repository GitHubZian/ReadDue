package com.zian.backend.reservation.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.catalog.application.CatalogService;
import com.zian.backend.catalog.domain.model.Book;
import com.zian.backend.iam.application.UserQueryService;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.reservation.domain.model.Reservation;
import com.zian.backend.reservation.domain.model.ReservationStatus;
import com.zian.backend.reservation.domain.repository.ReservationRepository;
import com.zian.backend.reservation.interfaces.dto.ReservationDtos;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预约应用服务：到书、过期、归还触发的自动顺延。
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;
    private final CatalogService catalogService;
    private final RuleService ruleService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;
    private final UserQueryService userQueryService;

    /**
     * 统计某书目的排队人数，用于续借限制。
     *
     * @param bookId 书目 ID
     * @return 排队人数
     */
    @Transactional(readOnly = true)
    public int countWaiting(Long bookId) {
        return repository.countWaiting(bookId);
    }

    /**
     * 归还后自动兑现：把队首预约置为到书待取并锁定副本。
     *
     * @param bookId 书目 ID
     * @return 被兑现的预约，无排队时为空
     */
    @Transactional
    public Optional<Reservation> promoteNext(Long bookId) {
        Optional<Reservation> next = repository.findNextWaiting(bookId);
        if (next.isEmpty()) {
            return Optional.empty();
        }
        Reservation reservation = next.get();
        int holdHours = ruleService.intValue("RESERVATION.READY_HOLD_HOURS", 48);
        reservation.markReady(LocalDateTime.now(), holdHours);
        if (repository.update(reservation) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "预约状态已被修改，请重试");
        }
        catalogService.holdForReservation(bookId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reservationId", reservation.getId());
        payload.put("bookId", bookId);
        payload.put("readerId", reservation.getReaderId());
        payload.put("expiresAt", String.valueOf(reservation.getExpiresAt()));
        outboxAppender.append("RESERVATION", reservation.getId(), MqConstant.EVENT_RESERVATION_READY, payload);
        return Optional.of(reservation);
    }

    /**
     * 管理员手动兑现预约，并补发到书通知。
     *
     * @param id 预约 ID
     * @return 预约聚合
     */
    @Transactional
    public Reservation promote(Long id) {
        Reservation reservation = require(id);
        if (reservation.getStatus() == com.zian.backend.reservation.domain.model.ReservationStatus.READY) {
            return reservation;
        }
        int holdHours = ruleService.intValue("RESERVATION.READY_HOLD_HOURS", 48);
        reservation.markReady(LocalDateTime.now(), holdHours);
        if (repository.update(reservation) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "预约状态已被修改，请重试");
        }
        catalogService.holdForReservation(reservation.getBookId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reservationId", reservation.getId());
        payload.put("bookId", reservation.getBookId());
        payload.put("readerId", reservation.getReaderId());
        payload.put("expiresAt", String.valueOf(reservation.getExpiresAt()));
        outboxAppender.append("RESERVATION", id, MqConstant.EVENT_RESERVATION_READY, payload);
        auditRecorder.record("RESERVATION_PROMOTE", "RESERVATION", id, "预约到书", Map.of("bookId", reservation.getBookId()));
        return reservation;
    }

    /**
     * 使预约过期并释放占位。
     *
     * @param id 预约 ID
     * @return 预约聚合
     */
    @Transactional
    public Reservation expire(Long id) {
        Reservation reservation = require(id);
        reservation.expire();
        if (repository.update(reservation) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "预约状态已被修改，请重试");
        }
        auditRecorder.record("RESERVATION_EXPIRE", "RESERVATION", id, "预约过期未取", Map.of());
        return reservation;
    }

    /**
     * 读取预约，不存在时抛 404。
     *
     * @param id 预约 ID
     * @return 预约聚合
     */
    @Transactional(readOnly = true)
    public Reservation require(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约记录不存在"));
    }

    /**
     * 创建预约：校验重复预约与在架副本后进入排队。
     *
     * @param bookId 书目 ID
     * @return 预约聚合
     */
    @Transactional
    public Reservation create(Long bookId) {
        Long readerId = UserContext.requireId();
        User reader = userQueryService.requireById(readerId);
        reader.assertCanBorrow("预约");
        Book book = catalogService.requireBook(bookId);
        if (repository.findActiveByReaderAndBook(readerId, bookId).isPresent()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "你已预约《" + book.title() + "》，请等待到书");
        }
        if (catalogService.hasAvailable(bookId)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该书目有在架副本，可直接到馆借阅");
        }
        int queueNo = repository.nextQueueNo(bookId);
        Reservation reservation = new Reservation(
                null, bookId, readerId, queueNo, ReservationStatus.WAITING, null, null, null, null, 0);
        Long id = repository.insert(reservation);
        auditRecorder.record(
                "RESERVATION_CREATE", "RESERVATION", id, "读者预约",
                Map.of("bookId", bookId, "queueNo", queueNo));
        return new Reservation(
                id, bookId, readerId, queueNo, ReservationStatus.WAITING, null, null, null, null, 0);
    }

    /**
     * 取消预约（本人或馆员）。
     *
     * @param id 预约 ID
     * @return 预约聚合
     */
    @Transactional
    public Reservation cancel(Long id) {
        Reservation reservation = require(id);
        LoginUser actor = UserContext.require();
        if (!actor.isStaff() && !actor.id().equals(reservation.getReaderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只能取消自己的预约");
        }
        reservation.cancel("读者取消");
        if (repository.update(reservation) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "预约状态已被修改，请重试");
        }
        auditRecorder.record("RESERVATION_CANCEL", "RESERVATION", id, "取消预约", Map.of());
        return reservation;
    }

    /**
     * 分页查询当前读者的预约。
     *
     * @param status    状态过滤
     * @param pageQuery 分页参数
     * @return 预约分页
     */
    @Transactional(readOnly = true)
    public PageResult<ReservationDtos.ReservationItem> myReservations(String status, PageQuery pageQuery) {
        Long readerId = UserContext.requireId();
        List<Reservation> reservations =
                repository.findByReader(readerId, status, (int) pageQuery.offset(), pageQuery.size());
        long total = repository.countByReader(readerId, status);
        Map<Long, Book> books = catalogService.booksByIds(
                reservations.stream().map(Reservation::getBookId).toList());
        List<ReservationDtos.ReservationItem> items = reservations.stream()
                .map(reservation -> {
                    Book book = books.get(reservation.getBookId());
                    return new ReservationDtos.ReservationItem(
                            reservation.getId(),
                            reservation.getBookId(),
                            book == null ? null : book.title(),
                            book == null ? null : book.author(),
                            reservation.getQueueNo(),
                            reservation.getStatus().name(),
                            Times.iso(reservation.getReadyAt()),
                            Times.iso(reservation.getExpiresAt()));
                })
                .toList();
        return PageResult.of(items, total, pageQuery);
    }
}
