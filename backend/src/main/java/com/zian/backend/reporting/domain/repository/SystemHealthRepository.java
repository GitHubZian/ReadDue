package com.zian.backend.reporting.domain.repository;

/**
 * 系统自检只读端口：供健康检查读取数据库元数据，避免 Controller 直接依赖 Mapper。
 */
public interface SystemHealthRepository {

    /**
     * 统计当前库中的基础表数量（不含视图）。
     *
     * @return 基础表数量；查询失败时由调用方按异常处理
     */
    Integer countBaseTables();
}
