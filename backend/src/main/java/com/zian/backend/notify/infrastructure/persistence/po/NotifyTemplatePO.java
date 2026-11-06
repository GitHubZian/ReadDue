package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code notify_template}（提醒文案模板（话术是交付物，需评审））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("notify_template")
@Data
public class NotifyTemplatePO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 如 DUE_L1 / DUE_L2 / DUE_L3 / GRACE_RESULT / APPEAL_SLA */
    private String templateCode;

    /** 枚举：INBOX/EMAIL/SMS/APP_PUSH */
    private String channel;

    /** 支持 {{bookTitle}} 等占位符 */
    private String titleTpl;

    private String bodyTpl;

    /** 非羞辱性措辞分级 */
    private String toneCode;

    @Version
    private String version;

    private Integer enabled;

    /** 0=未删除；删除时写入本行 id */
    private Long deleted;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    private LocalDateTime updatedAt;

}
