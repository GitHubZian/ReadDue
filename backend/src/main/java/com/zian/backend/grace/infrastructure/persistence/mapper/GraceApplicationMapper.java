package com.zian.backend.grace.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.grace.infrastructure.persistence.po.GraceApplicationPO;
import org.apache.ibatis.annotations.Mapper;

/** 宽限申请与计算快照（含可解释依据，24h 后清空敏感字段） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface GraceApplicationMapper extends BaseMapper<GraceApplicationPO> {
}
