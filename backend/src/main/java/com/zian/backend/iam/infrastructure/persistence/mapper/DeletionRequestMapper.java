package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.DeletionRequestPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 读者注销申请 Mapper。
 */
@Mapper
public interface DeletionRequestMapper extends BaseMapper<DeletionRequestPO> {
}
