package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code notify_delivery}（逐渠道投递结果（重试计数与失败归因））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("notify_delivery")
@Data
public class NotifyDeliveryPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    /** 枚举：INBOX/EMAIL/SMS/APP_PUSH */
    private String channel;

    /** 枚举：PENDING/SENT/FAILED/SKIPPED */
    private String status;

    private Integer attempt;

    private String providerMsgId;

    private String errorCode;

    private String errorMsg;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;

}
