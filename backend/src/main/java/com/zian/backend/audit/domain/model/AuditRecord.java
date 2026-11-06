package com.zian.backend.audit.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计事件（只追加，不修改）。
 */
public record AuditRecord(
        Long id,
        Long actorId,
        String actorRole,
        String action,
        String objectType,
        Long objectId,
        String purpose,
        Map<String, Object> detail,
        String ip,
        LocalDateTime createdAt) {

    public static AuditRecord of(
            Long actorId,
            String actorRole,
            String action,
            String objectType,
            Long objectId,
            String purpose,
            Map<String, Object> detail,
            String ip) {
        return new AuditRecord(null, actorId, actorRole, action, objectType, objectId, purpose, detail, ip, null);
    }
}
