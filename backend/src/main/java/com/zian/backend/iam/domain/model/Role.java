package com.zian.backend.iam.domain.model;

import java.util.List;

/**
 * 角色及其权限码。
 *
 * @param id 角色主键
 * @param code 角色编码（ADMIN / LIBRARIAN / READER）
 * @param name 角色名称
 * @param dataScope 数据范围（ALL / LIBRARY / SELF）
 * @param permissions 已授予的权限码，按权限排序号升序
 */
public record Role(long id, String code, String name, String dataScope, List<String> permissions) {}
