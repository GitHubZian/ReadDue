package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code user_status_log}（账号状态变更日志（只增，不修改））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("user_status_log")
@Data
public class UserStatusLogPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** NORMAL / BLACKLISTED / DELETED */
    private String fromStatus;

    private String toStatus;

    private String reason;

    /** 拉黑期限（仅转 BLACKLISTED 时） */
    private LocalDateTime expiresAt;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createdAt;

}
