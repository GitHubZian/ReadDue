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

    /**
     * 构造审计记录，未提供的字段取默认值。
     *
     * @param actorId    操作人 ID
     * @param actorRole  操作人角色
     * @param action     动作码
     * @param objectType 对象类型
     * @param objectId   对象 ID，可为 null
     * @param purpose    操作目的
     * @param detail     明细，可为 null
     * @param ip         来源 IP，可为 null
     * @return 审计记录
     */
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
