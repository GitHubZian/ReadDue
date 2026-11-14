package com.zian.backend.reservation.interfaces.dto;

import com.zian.backend.reservation.domain.model.Reservation;

/**
 * 预约写操作返回（列表读模型在 reporting）。
 */
public record ReservationActionView(Long id, String status, String readyAt, String expiresAt) {

    /**
     * 把预约聚合转换成管理端动作回执。
     *
     * @param reservation 预约
     * @return 动作回执
     */
    public static ReservationActionView of(Reservation reservation) {
        return new ReservationActionView(
                reservation.getId(),
                reservation.getStatus().name(),
                reservation.getReadyAt() == null ? null : reservation.getReadyAt().toString(),
                reservation.getExpiresAt() == null ? null : reservation.getExpiresAt().toString());
    }
}
