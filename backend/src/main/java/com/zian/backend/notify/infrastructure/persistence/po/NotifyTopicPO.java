package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 表 {@code notify_topic}（FR-11 通知主题）的 MyBatis-Plus 持久化对象。
 *
 * <p>渠道以 JSON 文本存放，由仓储层负责序列化，避免把 Jackson 类型带进应用层。
 */
@TableName("notify_topic")
@Data
public class NotifyTopicPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private String priority;

    private String defaultFrequency;

    /** JSON 数组文本，如 {@code ["INBOX","EMAIL"]} */
    private String defaultChannels;

    private Integer enabled;

    private Integer sort;
}
