package com.zian.backend.catalog.domain.model;

import java.time.LocalDate;

/**
 * 馆藏册行（只读）。
 *
 * <p>供读者端查看「有几册、在哪、当前状态」；字段名与查询列别名一致。
 *
 * @param id         册 ID
 * @param barcode    馆藏条码
 * @param location   馆藏位置
 * @param status     册状态：AVAILABLE/ON_LOAN/READY_SHELF/LOST/DAMAGED/REPAIRING/WITHDRAWN
 * @param acquiredAt 入藏日期
 */
public record CopyInfo(Long id, String barcode, String location, String status, LocalDate acquiredAt) {
}
