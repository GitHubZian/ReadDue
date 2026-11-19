package com.zian.backend.iam.domain.model;

/**
 * 权限目录项：供管理端勾选角色权限。
 *
 * @param id 权限主键
 * @param code 权限码（如 `catalog:read`）
 * @param name 权限名称
 * @param type 权限类型（MENU / BUTTON / API / DATA）
 * @param module 所属模块（由权限码前缀推导，如 `catalog`）
 */
public record PermissionInfo(long id, String code, String name, String type, String module) {}
