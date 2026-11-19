package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 申诉工单行：工单主体 + 读者 + 关联借阅的书目与副本。
 *
 * <p>关联借阅是左连接，{@code bookId}/{@code title}/{@code author}/{@code barcode} 可能为 null。
 *
 * @param id               工单主键
 * @param ticketNo         工单号
 * @param targetType       被申诉对象类型
 * @param reason           读者陈述
 * @param status           工单状态
 * @param createdAt        创建时间
 * @param updatedAt        最后更新时间
 * @param slaDueAt         SLA 截止时间
 * @param firstReplyAt     首次回复时间；未回复时为 null
 * @param resolution       处理结论；未结案时为 null
 * @param compensationDays 补偿天数
 * @param assigneeName     受理馆员署名；未受理时为 null
 * @param readerId         读者主键
 * @param readerName       读者姓名
 * @param userNo           学号/工号
 * @param department       院系
 * @param bookId           书目主键
 * @param title            书名
 * @param author           作者
 * @param barcode          副本条码
 */
public record AppealRow(
        Long id,
        String ticketNo,
        String targetType,
        String reason,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime slaDueAt,
        LocalDateTime firstReplyAt,
        String resolution,
        Integer compensationDays,
        String assigneeName,
        Long readerId,
        String readerName,
        String userNo,
        String department,
        Long bookId,
        String title,
        String author,
        String barcode) {
}
