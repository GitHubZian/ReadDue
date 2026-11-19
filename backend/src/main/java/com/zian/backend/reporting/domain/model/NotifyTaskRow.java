package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 通知任务行：提醒任务 + 读者姓名。
 *
 * @param id         任务主键
 * @param level      提醒级别
 * @param status     投递状态
 * @param createdAt  创建时间
 * @param planAt     计划发送时刻
 * @param updatedAt  最后更新时间
 * @param dedupKey   幂等键
 * @param readerName 读者姓名
 */
public record NotifyTaskRow(
        Long id,
        String level,
        String status,
        LocalDateTime createdAt,
        LocalDateTime planAt,
        LocalDateTime updatedAt,
        String dedupKey,
        String readerName) {
}
