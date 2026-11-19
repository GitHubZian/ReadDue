package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 表 {@code notify_preference}（读者通知偏好，只存差异）的 MyBatis-Plus 持久化对象。
 */
@TableName("notify_preference")
@Data
public class NotifyPreferencePO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long topicId;

    private String frequency;

    /** JSON 数组文本 */
    private String channels;
}
