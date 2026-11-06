package com.zian.backend.rule.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code academic_calendar}（校历场景区间（客观、零隐私成本））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("academic_calendar")
@Data
public class AcademicCalendarPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 学期，如 2026-FALL */
    private String term;

    /** 枚举：GRADUATION/EXAM/THESIS/NORMAL */
    private String sceneCode;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 枚举：CALENDAR/DECLARED */
    private String source;

    private String note;

    /** 0=未删除；删除时写入本行 id */
    private Long deleted;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    private LocalDateTime createdAt;

}
