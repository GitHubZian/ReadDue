package com.zian.backend.grace.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.grace.infrastructure.persistence.po.GraceEvidencePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@code grace_evidence} 表的数据访问入口。
 */
@Mapper
public interface GraceEvidenceMapper extends BaseMapper<GraceEvidencePO> {
}
