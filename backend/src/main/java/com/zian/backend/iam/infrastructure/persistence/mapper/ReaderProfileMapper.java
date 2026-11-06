package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.ReaderProfilePO;
import org.apache.ibatis.annotations.Mapper;

/** 读者档案与隐私授权状态 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface ReaderProfileMapper extends BaseMapper<ReaderProfilePO> {
}
