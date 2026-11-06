package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.UserStatusLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 账号状态变更日志（只增，不修改） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface UserStatusLogMapper extends BaseMapper<UserStatusLogPO> {
}
