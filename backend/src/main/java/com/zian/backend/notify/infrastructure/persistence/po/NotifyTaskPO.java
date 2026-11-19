package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * 表 {@code notify_task}（提醒任务（调度产物，幂等））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName(value = "notify_task", autoResultMap = true)
@Data
public class NotifyTaskPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** readerId:borrowOrderId:level:planDate，幂等键 */
    private String dedupKey;

    private Long readerId;

    private Long borrowOrderId;

    /** 枚举：L1/L2/L3/GRACE_RESULT/APPEAL_SLA/RESERVATION_READY/SYSTEM */
    private String level;

    /** 计划发送时刻（已含免打扰顺延） */
    private LocalDateTime planAt;

    /** 是否因免打扰顺延 */
    private Integer quietShifted;

    /** 枚举：PLANNED/QUEUED/SENT/PARTIAL/FAILED/CANCELED/MERGED */
    private String status;

    /** 渲染上下文（不含阅读内容） */
    @TableField(value = "payload", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    /** FR-11 主题编码，老数据为 null */
    private String topicCode;

    /** 排程时生效偏好快照：{frequency, channels, priority} */
    @TableField(value = "pref_snapshot", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> prefSnapshot;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
