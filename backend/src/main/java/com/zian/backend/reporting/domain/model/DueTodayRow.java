package com.zian.backend.reporting.domain.model;

import java.time.LocalDate;

/**
 * 今日到期借阅行：借阅单 + 读者 + 书目 + 副本条码。
 *
 * <p>只有 {@code department}/{@code author}/{@code barcode} 来自左连接，可能为 null。
 *
 * @param id             借阅单主键
 * @param readerId       读者主键
 * @param readerName     读者姓名
 * @param userNo         学号/工号
 * @param department     院系
 * @param bookId         书目主键
 * @param title          书名
 * @param author         作者
 * @param barcode        副本条码
 * @param effectiveDueAt 应还日期（已含宽限）
 */
public record DueTodayRow(
        Long id,
        Long readerId,
        String readerName,
        String userNo,
        String department,
        Long bookId,
        String title,
        String author,
        String barcode,
        LocalDate effectiveDueAt) {
}
