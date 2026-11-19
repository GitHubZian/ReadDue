package com.zian.backend.reporting.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 逾期看板行：逾期视图 + 借阅状态 + L3 提醒与副本补充信息。
 *
 * <p>{@code lastReminded}/{@code department}/{@code barcode}/{@code author} 来自子查询或左连接，可能为 null。
 *
 * @param orderId        借阅单主键
 * @param readerId       读者主键
 * @param readerName     读者姓名
 * @param studentNo      学号
 * @param bookId         书目主键
 * @param bookTitle      书名
 * @param effectiveDueAt 应还日期
 * @param overdueDays    逾期天数
 * @param l3Sent         已发送的 L3 提醒次数
 * @param status         借阅单状态（LOST 或 BORROWED/OVERDUE）
 * @param lastReminded   最近一次 L3 提醒时间
 * @param department     院系
 * @param barcode        副本条码
 * @param author         作者
 */
public record OverdueRow(
        Long orderId,
        Long readerId,
        String readerName,
        String studentNo,
        Long bookId,
        String bookTitle,
        LocalDate effectiveDueAt,
        Integer overdueDays,
        Long l3Sent,
        String status,
        LocalDateTime lastReminded,
        String department,
        String barcode,
        String author) {
}
