package com.zian.backend.notify.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知任务（贫血）：一条待投递的站内信/邮件。
 *
 * <p>{@code topicCode} 与 {@code prefSnapshot} 记录排程时刻主题生效的偏好快照，
 * 后续读者改偏好不会追溯已排程任务（FR-11）。
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
        String topicCode,
        Map<String, Object> prefSnapshot,
        LocalDateTime createdAt) {
}
