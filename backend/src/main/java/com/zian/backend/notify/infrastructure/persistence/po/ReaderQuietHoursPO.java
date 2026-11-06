package com.zian.backend.notify.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

/**
 * 表 {@code reader_quiet_hours}（免打扰时段，命中后顺延至结束时刻合并推送）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("reader_quiet_hours")
@Data
public class ReaderQuietHoursPO {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 免打扰开关 */
    private Integer enabled;

    /** 免打扰开始 */
    private LocalTime startTime;

    /** 免打扰结束（跨零点） */
    private LocalTime endTime;

    private LocalDateTime updatedAt;

}
