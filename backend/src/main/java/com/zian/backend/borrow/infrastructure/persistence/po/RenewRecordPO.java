package com.zian.backend.borrow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code renew_record}（续借记录（含拒绝理由，支撑「为什么不能续借」））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("renew_record")
@Data
public class RenewRecordPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long borrowOrderId;

    /** 第几次续借 */
    private Integer seq;

    private LocalDate oldDueAt;

    private LocalDate newDueAt;

    /** 枚举：APPROVED/REJECTED */
    private String result;

    /** QUEUE_LIMIT / RENEW_LIMIT / OVERDUE / RESERVED / HOLDING */
    private String rejectCode;

    /** 决策时预约队列长度（公平性证据） */
    private Integer queueLen;

    /** 枚举：READER/LIBRARIAN/SYSTEM */
    private String operatorType;

    private Long operatorId;

    private LocalDateTime createdAt;

}
