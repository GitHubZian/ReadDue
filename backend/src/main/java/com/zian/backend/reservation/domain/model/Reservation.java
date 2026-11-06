package com.zian.backend.reservation.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDateTime;

/**
 * 预约聚合：队列号 + 到书保留 48 小时 + 过期顺延。
 */
public class Reservation {

    private final Long id;
    private final Long bookId;
    private final Long readerId;
    private final int queueNo;
    private ReservationStatus status;
    private LocalDateTime readyAt;
    private LocalDateTime expiresAt;
    private LocalDateTime fulfilledAt;
    private String cancelReason;
    private final Integer version;

    public Reservation(
            Long id,
            Long bookId,
            Long readerId,
            int queueNo,
            ReservationStatus status,
            LocalDateTime readyAt,
            LocalDateTime expiresAt,
            LocalDateTime fulfilledAt,
            String cancelReason,
            Integer version) {
        this.id = id;
        this.bookId = bookId;
        this.readerId = readerId;
        this.queueNo = queueNo;
        this.status = status;
        this.readyAt = readyAt;
        this.expiresAt = expiresAt;
        this.fulfilledAt = fulfilledAt;
        this.cancelReason = cancelReason;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public Long getReaderId() {
        return readerId;
    }

    public int getQueueNo() {
        return queueNo;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getFulfilledAt() {
        return fulfilledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    /** 到书：进入 READY，保留 holdHours 小时。 */
    public void markReady(LocalDateTime now, int holdHours) {
        if (status != ReservationStatus.WAITING) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该预约当前状态为「" + status + "」，无法到书");
        }
        this.status = ReservationStatus.READY;
        this.readyAt = now;
        this.expiresAt = now.plusHours(holdHours);
    }

    /** 过期未取：释放名额。 */
    public void expire() {
        if (status != ReservationStatus.READY) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "只有待到书状态可以标记过期");
        }
        this.status = ReservationStatus.EXPIRED;
    }

    /** 读者取书。 */
    public void fulfill(LocalDateTime now) {
        if (status != ReservationStatus.READY) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该预约不在待到书状态");
        }
        this.status = ReservationStatus.FULFILLED;
        this.fulfilledAt = now;
    }

    public void cancel(String reason) {
        if (status == ReservationStatus.FULFILLED || status == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该预约已结束，无法取消");
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
    }
}
