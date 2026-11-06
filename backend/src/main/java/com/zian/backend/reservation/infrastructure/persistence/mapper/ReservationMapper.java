package com.zian.backend.reservation.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.reservation.infrastructure.persistence.po.ReservationPO;
import org.apache.ibatis.annotations.Mapper;

/** 预约队列（Kq 供需系数来源） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface ReservationMapper extends BaseMapper<ReservationPO> {
}
