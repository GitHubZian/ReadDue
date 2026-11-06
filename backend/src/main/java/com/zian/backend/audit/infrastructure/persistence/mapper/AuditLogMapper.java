package com.zian.backend.audit.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.audit.infrastructure.persistence.po.AuditLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 操作审计，保留 90 天，仅供追责 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogPO> {
}
