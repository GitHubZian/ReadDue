package com.zian.backend.notify.domain.model;

/**
 * 通知任务状态（与 notify_task.status 一致）。
 */
public enum NotifyTaskStatus {
    PLANNED,
    QUEUED,
    SENT,
    PARTIAL,
    FAILED,
    CANCELED,
    MERGED
}
