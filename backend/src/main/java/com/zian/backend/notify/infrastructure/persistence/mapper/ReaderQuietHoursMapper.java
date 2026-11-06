package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.ReaderQuietHoursPO;
import org.apache.ibatis.annotations.Mapper;

/** 免打扰时段，命中后顺延至结束时刻合并推送 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface ReaderQuietHoursMapper extends BaseMapper<ReaderQuietHoursPO> {
}
