package com.zian.backend.shared.outbox.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.shared.outbox.infrastructure.persistence.po.EventOutboxPO;
import org.apache.ibatis.annotations.Mapper;

/** 本地消息表：业务事务内落库，再由投递器发 RabbitMQ（至少一次） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface EventOutboxMapper extends BaseMapper<EventOutboxPO> {
}
