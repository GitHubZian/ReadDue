package com.zian.backend.notify.domain.model;

import java.time.LocalDateTime;

/**
 * 提醒文案模板（话术是交付物，修改需审计）。
 */
public record NotifyTemplate(
        Long id,
        String templateCode,
        String channel,
        String titleTpl,
        String bodyTpl,
        String toneCode,
        String version,
        boolean enabled,
        LocalDateTime updatedAt) {
}
