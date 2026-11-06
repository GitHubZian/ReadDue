package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyTaskPO;
import org.apache.ibatis.annotations.Mapper;

/** 提醒任务（调度产物，幂等） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface NotifyTaskMapper extends BaseMapper<NotifyTaskPO> {
}
