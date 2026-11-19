package com.zian.backend.catalog.domain.model;

/**
 * 读者在指定书目上的预约状态（只读）。
 *
 * @param queueNo 排队序号
 * @param status  预约状态：WAITING/READY
 */
public record MyReservation(Integer queueNo, String status) {
}
