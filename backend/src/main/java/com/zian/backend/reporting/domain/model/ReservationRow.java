package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 预约行：预约队列 + 读者 + 书目。
 *
 * @param id        预约主键
 * @param queueNo   排队序号
 * @param status    预约状态
 * @param createdAt 预约时间
 * @param readyAt   到馆可取时间；未到书时为 null
 * @param expiresAt 保留截止时间；未到书时为 null
 * @param readerId  读者主键
 * @param readerName 读者姓名
 * @param userNo    学号/工号
 * @param department 院系
 * @param bookId    书目主键
 * @param title     书名
 * @param author    作者
 */
public record ReservationRow(
        Long id,
        Integer queueNo,
        String status,
        LocalDateTime createdAt,
        LocalDateTime readyAt,
        LocalDateTime expiresAt,
        Long readerId,
        String readerName,
        String userNo,
        String department,
        Long bookId,
        String title,
        String author) {
}
