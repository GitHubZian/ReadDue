package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

/**
 * 表 {@code reader_timetable}（读者课表，M5-F）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层。
 */
@TableName("reader_timetable")
@Data
public class TimetablePO {

    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 读者 id */
    private Long userId;

    /** 星期：1=周一 … 7=周日 */
    private Integer weekday;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 软删除标记：0=有效，1=已删除 */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
