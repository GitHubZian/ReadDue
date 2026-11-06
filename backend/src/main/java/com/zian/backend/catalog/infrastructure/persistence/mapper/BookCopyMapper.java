package com.zian.backend.catalog.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.catalog.infrastructure.persistence.po.BookCopyPO;
import org.apache.ibatis.annotations.Mapper;

/** 馆藏册 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface BookCopyMapper extends BaseMapper<BookCopyPO> {
}
