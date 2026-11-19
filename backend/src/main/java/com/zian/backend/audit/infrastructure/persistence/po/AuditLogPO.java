package com.zian.backend.audit.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * 表 {@code audit_log}（操作审计，保留 90 天，仅供追责）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName(value = "audit_log", autoResultMap = true)
@Data
public class AuditLogPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long actorId;

    private String actorRole;

    /** GRACE_SNAPSHOT_READ / CONSENT_REVOKED / TIER_OVERRIDE ... */
    private String action;

    private String objectType;

    private Long objectId;

    /** 追责用途标签，禁止用于画像 */
    private String purpose;

    @TableField(value = "detail", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> detail;

    private String ip;

    /** 当次请求的 X-Trace-Id，用于把前端报障与审计行对齐 */
    private String traceId;

    /** 变更前快照；只有宽限审批、申诉处理、规则调整等留痕操作才有值 */
    @TableField(value = "before_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> beforeJson;

    /** 变更后快照；与 beforeJson 成对出现 */
    @TableField(value = "after_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> afterJson;

    private LocalDateTime createdAt;

}
