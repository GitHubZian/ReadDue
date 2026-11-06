package com.zian.backend.notify.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知任务（贫血）：一条待投递的站内信/邮件。
 */
public record NotifyTask(
        Long id,
        String dedupKey,
        Long readerId,
        Long borrowOrderId,
        String level,
        LocalDateTime planAt,
        boolean quietShifted,
        NotifyTaskStatus status,
        Map<String, Object> payload,
        LocalDateTime createdAt) {
}
