package com.zian.backend.bootstrap.web;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 启动自检用的只读探针：只查元数据，不触碰业务表。
 */
@Mapper
public interface SystemHealthMapper {

    @Select("""
            SELECT COUNT(*)
              FROM information_schema.TABLES
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_TYPE = 'BASE TABLE'
            """)
    Integer countBaseTables();
}
