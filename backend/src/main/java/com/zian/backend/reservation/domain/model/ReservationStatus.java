package com.zian.backend.reservation.domain.model;

/**
 * 预约状态。
 */
public enum ReservationStatus {
    WAITING,
    READY,
    FULFILLED,
    CANCELLED,
    EXPIRED
}
