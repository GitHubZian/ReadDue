package com.zian.backend.rule.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.rule.infrastructure.persistence.po.AcademicCalendarPO;
import org.apache.ibatis.annotations.Mapper;

/** 校历场景区间（客观、零隐私成本） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface AcademicCalendarMapper extends BaseMapper<AcademicCalendarPO> {
}
