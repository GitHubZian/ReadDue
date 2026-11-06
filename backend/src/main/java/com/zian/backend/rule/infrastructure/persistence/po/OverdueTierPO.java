package com.zian.backend.rule.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.util.List;
import lombok.Data;

/**
 * 表 {@code overdue_tier}（逾期分级配置（FR-04，每层动作与话术不同））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName(value = "overdue_tier", autoResultMap = true)
@Data
public class OverdueTierPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** T1 / T2 / T3 / T4 / T5 */
    private String tierCode;

    /** 如「温和提醒期」 */
    private String tierName;

    private Integer minDays;

    /** NULL 表示无上限 */
    private Integer maxDays;

    /** REINFORCE / APPEAL_WINDOW / LIMIT_BORROW / LIBRARIAN_HANDLE */
    private String actionCode;

    /** message-soft / message-clear / message-formal，对应话术模板 */
    private String toneCode;

    /** 该层是否仍可续借 */
    private Integer allowRenew;

    /** 该层默认渠道 */
    @TableField(value = "channels", typeHandler = JacksonTypeHandler.class)
    private List<String> channels;

    @Version
    private String version;

    private String description;

}
