package com.zian.backend.rule.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.rule.infrastructure.persistence.po.RuleParamPO;
import org.apache.ibatis.annotations.Mapper;

/** 规则参数表（参数即产品，公开可核对） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface RuleParamMapper extends BaseMapper<RuleParamPO> {
}
