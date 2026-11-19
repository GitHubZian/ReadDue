package com.zian.backend.iam.infrastructure.persistence.mapper;

/**
 * 角色基础信息的查询行（不含权限码）。
 *
 * @param id 角色主键
 * @param code 角色编码
 * @param name 角色名称
 * @param dataScope 数据范围
 */
public record RoleRow(long id, String code, String name, String dataScope) {}
