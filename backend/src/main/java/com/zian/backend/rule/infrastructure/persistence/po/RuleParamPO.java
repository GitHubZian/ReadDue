package com.zian.backend.rule.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code rule_param}（规则参数表（参数即产品，公开可核对））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("rule_param")
@Data
public class RuleParamPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 如 BS.GRADUATION / KT_TIER_1 / QUIET.START */
    private String paramKey;

    private String paramValue;

    /** 枚举：INT/DECIMAL/STRING/TIME/JSON */
    private String valueType;

    @Version
    private String version;

    private LocalDateTime effectiveFrom;

    /** NULL 表示当前生效 */
    private LocalDateTime effectiveTo;

    /** 对读者公示的参数说明 */
    private String description;

    private Long updatedBy;

    private LocalDateTime updatedAt;

}
