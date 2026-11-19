package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyTopicPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知主题 Mapper。
 */
@Mapper
public interface NotifyTopicMapper extends BaseMapper<NotifyTopicPO> {}
