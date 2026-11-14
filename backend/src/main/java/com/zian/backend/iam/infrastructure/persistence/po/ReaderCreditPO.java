package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code reader_credit}（守信系数 Kt 的物化输入（不从借阅记录实时聚合））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("reader_credit")
@Data
public class ReaderCreditPO {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 近 12 个月逾期次数 */
    @TableField("overdue_cnt_12m")
    private Integer overdueCnt12m;

    /** 连续守信月数（满 6 恢复一档） */
    private Integer consecutiveGoodMonths;

    /** 守信系数档位 0=1.00 / 1=0.85 / 2=0.70 / 3=0.55 */
    private Integer ktTier;

    private LocalDateTime lastEvaluatedAt;

    private LocalDateTime updatedAt;

}
