package com.zian.backend.appeal.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.appeal.infrastructure.persistence.po.AppealEvidencePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@code appeal_evidence} 表的数据访问入口。
 */
@Mapper
public interface AppealEvidenceMapper extends BaseMapper<AppealEvidencePO> {
}
