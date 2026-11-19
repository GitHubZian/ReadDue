package com.zian.backend.iam.interfaces.dto;

import com.zian.backend.iam.domain.model.PermissionInfo;
import com.zian.backend.iam.domain.model.Role;
import java.util.List;

/**
 * 角色与权限管理的请求 / 响应体。
 */
public final class RoleDtos {

    private RoleDtos() {}

    /**
     * 角色视图。
     *
     * @param code 角色编码
     * @param name 角色名称
     * @param dataScope 数据范围
     * @param permissions 已授予的权限码
     * @param permissionCount 权限数量
     */
    public record RoleView(String code, String name, String dataScope, List<String> permissions,
                           int permissionCount) {

        /**
         * 由领域角色转为视图。
         *
         * @param role 角色
         * @return 角色视图
         */
        public static RoleView of(Role role) {
            return new RoleView(role.code(), role.name(), role.dataScope(), role.permissions(),
                    role.permissions().size());
        }
    }

    /**
     * 权限目录项视图。
     *
     * @param id 权限主键
     * @param code 权限码
     * @param name 权限名称
     * @param type 权限类型
     * @param module 所属模块
     */
    public record PermissionView(String id, String code, String name, String type, String module) {

        /**
         * 由领域权限转为视图。
         *
         * @param info 权限
         * @return 权限视图
         */
        public static PermissionView of(PermissionInfo info) {
            return new PermissionView(String.valueOf(info.id()), info.code(), info.name(),
                    info.type(), info.module());
        }
    }

    /**
     * 替换角色权限的请求体。
     *
     * @param permissions 新的权限码全集（全量替换）
     */
    public record UpdatePermissionsRequest(List<String> permissions) {}

    /**
     * 替换权限后的结果。
     *
     * @param code 角色编码
     * @param permissions 生效的权限码
     */
    public record UpdateResult(String code, List<String> permissions) {}
}
