package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code notify_preference_log}（偏好变更日志）的 MyBatis-Plus 持久化对象。
 */
@TableName("notify_preference_log")
@Data
public class NotifyPreferenceLogPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String topicCode;

    /** 变更前偏好 JSON，首次设置为 null */
    private String beforeJson;

    /** 变更后偏好 JSON，恢复默认为 null */
    private String afterJson;

    private Long operatorId;

    private LocalDateTime createdAt;
}
