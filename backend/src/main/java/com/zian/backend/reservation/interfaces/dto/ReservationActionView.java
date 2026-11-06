package com.zian.backend.reservation.interfaces.dto;

import com.zian.backend.reservation.domain.model.Reservation;

/**
 * 预约写操作返回（列表读模型在 reporting）。
 */
public record ReservationActionView(Long id, String status, String readyAt, String expiresAt) {

    public static ReservationActionView of(Reservation reservation) {
        return new ReservationActionView(
                reservation.getId(),
                reservation.getStatus().name(),
                reservation.getReadyAt() == null ? null : reservation.getReadyAt().toString(),
                reservation.getExpiresAt() == null ? null : reservation.getExpiresAt().toString());
    }
}
