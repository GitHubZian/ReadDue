package com.zian.backend.borrow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.borrow.infrastructure.persistence.po.RenewRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 续借记录（含拒绝理由，支撑「为什么不能续借」） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface RenewRecordMapper extends BaseMapper<RenewRecordPO> {
}
