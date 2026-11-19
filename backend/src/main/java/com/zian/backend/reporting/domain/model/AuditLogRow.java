package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 审计日志行：操作记录 + 操作人姓名。
 *
 * @param id         日志主键
 * @param action     动作码
 * @param objectType 对象类型
 * @param objectId   对象主键
 * @param purpose    追责用途标签
 * @param detail     变更详情（JSON 原文）；无详情时为 null
 * @param ip         来源 IP
 * @param traceId    请求 traceId；旧数据为 null
 * @param beforeJson 变更前快照（JSON 原文）；无留痕时为 null
 * @param afterJson  变更后快照（JSON 原文）；无留痕时为 null
 * @param actorRole  操作人角色
 * @param createdAt  操作时间
 * @param actorName  操作人姓名；系统操作或用户已删除时为 null
 */
public record AuditLogRow(
        Long id,
        String action,
        String objectType,
        Long objectId,
        String purpose,
        String detail,
        String ip,
        String traceId,
        String beforeJson,
        String afterJson,
        String actorRole,
        LocalDateTime createdAt,
        String actorName) {
}
