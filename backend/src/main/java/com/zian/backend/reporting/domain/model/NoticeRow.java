package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 工作台通知行：最近已送达的提醒任务与读者姓名。
 *
 * @param id         通知任务主键
 * @param level      提醒级别（L1/L2/L3/GRACE_RESULT/…）
 * @param planAt     计划发送时刻
 * @param readerName 读者姓名；读者已逻辑删除时为 null
 */
public record NoticeRow(Long id, String level, LocalDateTime planAt, String readerName) {
}
