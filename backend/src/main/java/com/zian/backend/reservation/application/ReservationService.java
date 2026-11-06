package com.zian.backend.reservation.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.catalog.application.CatalogService;
import com.zian.backend.reservation.domain.model.Reservation;
import com.zian.backend.reservation.domain.repository.ReservationRepository;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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

    /**
     * 归还触发：队首读者到书，保留 48 小时并预留在架副本。
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

    /** 管理员手动到书。 */
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
        auditRecorder.record("RESERVATION_PROMOTE", "RESERVATION", id, "预约到书", Map.of("bookId", reservation.getBookId()));
        return reservation;
    }

    /** 管理员标记过期（读者 48h 未取）。 */
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

    @Transactional(readOnly = true)
    public Reservation require(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约记录不存在"));
    }
}
