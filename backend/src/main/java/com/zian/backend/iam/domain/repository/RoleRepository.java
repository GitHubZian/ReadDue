package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.PermissionInfo;
import com.zian.backend.iam.domain.model.Role;
import java.util.List;
import java.util.Optional;

/**
 * 角色与角色-权限关联的管理端口。
 */
public interface RoleRepository {

    /**
     * 列出全部启用角色及其权限码。
     *
     * @return 角色列表
     */
    List<Role> findAll();

    /**
     * 按编码查角色。
     *
     * @param code 角色编码
     * @return 角色
     */
    Optional<Role> findByCode(String code);

    /**
     * 权限目录（全部启用权限）。
     *
     * @return 权限列表
     */
    List<PermissionInfo> allPermissions();

    /**
     * 用给定权限替换角色的全部权限（先删后插）。
     *
     * @param roleId 角色主键
     * @param permissionIds 权限主键集合
     */
    void replacePermissions(long roleId, List<Long> permissionIds);
}
