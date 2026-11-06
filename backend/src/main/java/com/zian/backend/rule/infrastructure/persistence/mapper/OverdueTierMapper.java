package com.zian.backend.rule.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.rule.infrastructure.persistence.po.OverdueTierPO;
import org.apache.ibatis.annotations.Mapper;

/** 逾期分级配置（FR-04，每层动作与话术不同） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface OverdueTierMapper extends BaseMapper<OverdueTierPO> {
}
