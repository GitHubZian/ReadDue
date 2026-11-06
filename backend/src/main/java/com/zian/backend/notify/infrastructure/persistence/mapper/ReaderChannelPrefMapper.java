package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.ReaderChannelPrefPO;
import org.apache.ibatis.annotations.Mapper;

/** 提醒渠道偏好（FR-01） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface ReaderChannelPrefMapper extends BaseMapper<ReaderChannelPrefPO> {
}
