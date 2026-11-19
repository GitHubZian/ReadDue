package com.zian.backend.audit.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计事件（只追加，不修改）。
 *
 * <p>可追溯信息：{@code traceId} 与当次请求的 {@code X-Trace-Id} 一致；
 * {@code before}/{@code after} 只在需要留痕的写操作（宽限审批、申诉处理、规则调整）写入，
 * 其余操作为 null。四个 JSON 字段都只能放标量，禁止塞入实体或时间对象。
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
        String traceId,
        Map<String, Object> before,
        Map<String, Object> after,
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
        return of(actorId, actorRole, action, objectType, objectId, purpose, detail, ip, null, null, null);
    }

    /**
     * 构造带链路与前后值的审计记录。
     *
     * @param actorId    操作人 ID
     * @param actorRole  操作人角色
     * @param action     动作码
     * @param objectType 对象类型
     * @param objectId   对象 ID，可为 null
     * @param purpose    操作目的
     * @param detail     明细，可为 null
     * @param ip         来源 IP，可为 null
     * @param traceId    请求 traceId，可为 null
     * @param before     变更前快照，可为 null
     * @param after      变更后快照，可为 null
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
            String ip,
            String traceId,
            Map<String, Object> before,
            Map<String, Object> after) {
        return new AuditRecord(
                null, actorId, actorRole, action, objectType, objectId, purpose, detail, ip,
                traceId, before, after, null);
    }
}
