package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 今日流通流水行：借出与归还共用一种投影，{@code type} 区分方向。
 *
 * @param id         借阅单主键
 * @param at         发生时刻（借出或归还）
 * @param type       流水类型，BORROW 或 RETURN
 * @param readerId   读者主键
 * @param readerName 读者姓名
 * @param userNo     学号/工号
 * @param department 院系
 * @param bookId     书目主键
 * @param title      书名
 * @param author     作者
 * @param barcode    副本条码
 */
public record CirculationRow(
        Long id,
        LocalDateTime at,
        String type,
        Long readerId,
        String readerName,
        String userNo,
        String department,
        Long bookId,
        String title,
        String author,
        String barcode) {
}
