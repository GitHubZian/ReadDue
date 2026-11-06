package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyDeliveryPO;
import org.apache.ibatis.annotations.Mapper;

/** 逐渠道投递结果（重试计数与失败归因） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface NotifyDeliveryMapper extends BaseMapper<NotifyDeliveryPO> {
}
