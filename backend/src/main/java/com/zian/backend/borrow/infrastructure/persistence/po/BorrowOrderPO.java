package com.zian.backend.borrow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code borrow_order}（借阅单（核心表））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("borrow_order")
@Data
public class BorrowOrderPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 借阅单号 */
    private String orderNo;

    private Long readerId;

    /** 冗余书目，避免看板联表 */
    private Long bookId;

    private Long copyId;

    private LocalDateTime borrowedAt;

    /** 应还日 */
    private LocalDate dueAt;

    /** 含宽限的最终应还日 = due_at + grace_days */
    private LocalDate effectiveDueAt;

    /** 已批准宽限天数 */
    private Integer graceDays;

    /** 已续借次数 */
    private Integer renewCount;

    private LocalDateTime returnedAt;

    /** 归还/当前逾期天数快照 */
    private Integer finalOverdueDays;

    /** 借阅状态机 枚举：BORROWED/OVERDUE/RETURNED/LOST/CLOSED */
    private String status;

    /** 归还验收结论 枚举：GOOD/DAMAGED/LOST */
    private String returnCondition;

    /** 乐观锁版本号（续借/归还/宽限并发） */
    @Version
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
