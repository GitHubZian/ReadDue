package com.zian.backend.appeal.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.appeal.infrastructure.persistence.po.AppealEventPO;
import org.apache.ibatis.annotations.Mapper;

/** 工单流转时间线（读者可见全过程） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface AppealEventMapper extends BaseMapper<AppealEventPO> {
}
