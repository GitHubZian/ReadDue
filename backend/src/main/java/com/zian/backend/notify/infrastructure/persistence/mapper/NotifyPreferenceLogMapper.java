package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyPreferenceLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 偏好变更日志 Mapper。
 */
@Mapper
public interface NotifyPreferenceLogMapper extends BaseMapper<NotifyPreferenceLogPO> {}
