package com.zian.backend.appeal.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code appeal_event}（工单流转时间线（读者可见全过程））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("appeal_event")
@Data
public class AppealEventPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long ticketId;

    /** 枚举：CREATED/ASSIGNED/REPLIED/RESOLVED/REJECTED/AUTO_COMPENSATED/CLOSED */
    private String eventType;

    private Long actorId;

    private String actorName;

    private String content;

    private LocalDateTime createdAt;

}
