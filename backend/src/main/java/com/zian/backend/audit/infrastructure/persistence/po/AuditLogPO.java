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

    private LocalDateTime createdAt;

}
