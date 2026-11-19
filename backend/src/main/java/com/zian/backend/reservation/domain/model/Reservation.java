package com.zian.backend.reservation.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预约聚合：队列号 + 到书保留 48 小时 + 过期顺延。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    private Long id;
    private Long bookId;
    private Long readerId;
    private int queueNo;
    private ReservationStatus status;
    private LocalDateTime readyAt;
    private LocalDateTime expiresAt;
    private LocalDateTime fulfilledAt;
    private String cancelReason;
    private Integer version;

    /**
     * 是否仍在排队。
     */
    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    /**
     * 到书：WAITING → READY，并设置取书截止时间。
     *
     * @param now       到书时间
     * @param holdHours 保留小时数
     */
    public void markReady(LocalDateTime now, int holdHours) {
        if (status != ReservationStatus.WAITING) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该预约当前状态为「" + status + "」，无法到书");
        }
        this.status = ReservationStatus.READY;
        this.readyAt = now;
        this.expiresAt = now.plusHours(holdHours);
    }

    /**
     * 过期：仅 READY 可过期。
     */
    public void expire() {
        if (status != ReservationStatus.READY) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "只有待到书状态可以标记过期");
        }
        this.status = ReservationStatus.EXPIRED;
    }

    /**
     * 取书完成：READY → FULFILLED。
     *
     * @param now 取书时间
     */
    public void fulfill(LocalDateTime now) {
        if (status != ReservationStatus.READY) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该预约不在待到书状态");
        }
        this.status = ReservationStatus.FULFILLED;
        this.fulfilledAt = now;
    }

    /**
     * 取消预约：仅 WAITING/READY 可取消。
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        if (status != ReservationStatus.WAITING && status != ReservationStatus.READY) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该预约已结束，无法取消");
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
    }
}
