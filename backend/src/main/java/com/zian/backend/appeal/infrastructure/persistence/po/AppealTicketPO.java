package com.zian.backend.appeal.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code appeal_ticket}（申诉工单）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("appeal_ticket")
@Data
public class AppealTicketPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String ticketNo;

    private Long readerId;

    /** 枚举：GRACE/OVERDUE_TIER/RENEW/REMINDER/OTHER */
    private String targetType;

    /** 被申诉对象主键（grace_application / borrow_order / notify_task） */
    private Long targetId;

    /** 读者陈述 */
    private String reason;

    /** 枚举：OPEN/ACKED/RESOLVED/REJECTED/AUTO_COMPENSATED/CLOSED */
    private String status;

    /** 创建时间 + 2h */
    private LocalDateTime slaDueAt;

    private LocalDateTime firstReplyAt;

    /** 是否超时（超时自动补偿 3 天） */
    private Integer slaBreached;

    private Long assigneeId;

    /** 受理馆员真实署名 */
    private String assigneeName;

    private Integer compensationDays;

    private String resolution;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
