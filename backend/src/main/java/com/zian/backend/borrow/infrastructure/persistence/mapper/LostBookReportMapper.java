package com.zian.backend.borrow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.borrow.infrastructure.persistence.po.LostBookReportPO;
import org.apache.ibatis.annotations.Mapper;

/** 丢书/损书申报（人工确认，不做自动扣款） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface LostBookReportMapper extends BaseMapper<LostBookReportPO> {
}
