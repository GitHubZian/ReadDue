package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.SysUserPO;
import org.apache.ibatis.annotations.Mapper;

/** 账号（读者/馆员/管理员） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserPO> {
}
