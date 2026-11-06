package com.zian.backend.shared.outbox.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * 表 {@code event_outbox}（本地消息表：业务事务内落库，再由投递器发 RabbitMQ（至少一次））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName(value = "event_outbox", autoResultMap = true)
@Data
public class EventOutboxPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** BORROW_ORDER / GRACE / APPEAL / RESERVATION */
    private String aggregateType;

    private Long aggregateId;

    /** BORROW_RETURNED / OVERDUE_MARKED / GRACE_APPROVED ... */
    private String eventType;

    @TableField(value = "payload", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    /** 枚举：NEW/PUBLISHED/FAILED/DEAD */
    private String status;

    private Integer retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

}
