package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyPreferencePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 读者通知偏好 Mapper。
 */
@Mapper
public interface NotifyPreferenceMapper extends BaseMapper<NotifyPreferencePO> {}
