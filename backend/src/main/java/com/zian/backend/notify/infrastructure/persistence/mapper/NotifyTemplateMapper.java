package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyTemplatePO;
import org.apache.ibatis.annotations.Mapper;

/** 提醒文案模板（话术是交付物，需评审） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface NotifyTemplateMapper extends BaseMapper<NotifyTemplatePO> {
}
