package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.ReaderCreditPO;
import org.apache.ibatis.annotations.Mapper;

/** 守信系数 Kt 的物化输入（不从借阅记录实时聚合） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface ReaderCreditMapper extends BaseMapper<ReaderCreditPO> {
}
