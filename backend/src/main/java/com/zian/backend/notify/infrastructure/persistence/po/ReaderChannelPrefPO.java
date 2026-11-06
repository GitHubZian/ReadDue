package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code reader_channel_pref}（提醒渠道偏好（FR-01））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("reader_channel_pref")
@Data
public class ReaderChannelPrefPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** 站内 / 邮件 / 短信 / App 推送 枚举：INBOX/EMAIL/SMS/APP_PUSH */
    private String channel;

    /** 渠道开关 */
    private Integer enabled;

    private LocalDateTime updatedAt;

}
