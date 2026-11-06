package com.zian.backend.borrow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code lost_book_report}（丢书/损书申报（人工确认，不做自动扣款））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("lost_book_report")
@Data
public class LostBookReportPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long borrowOrderId;

    private Long readerId;

    /** 枚举：LOST/DAMAGED */
    private String declaredType;

    private String description;

    /** 枚举：SUBMITTED/CONFIRMED/COMPENSATED/REJECTED */
    private String status;

    private String handlingNote;

    private Long decidedBy;

    private LocalDateTime decidedAt;

    private LocalDateTime createdAt;

}
