package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.TimetablePO;
import org.apache.ibatis.annotations.Mapper;

/** 读者课表的 MyBatis-Plus Mapper（M5-F）。 */
@Mapper
public interface TimetableMapper extends BaseMapper<TimetablePO> {
}
