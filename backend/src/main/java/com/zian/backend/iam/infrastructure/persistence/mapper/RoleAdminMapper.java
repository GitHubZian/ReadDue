package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.zian.backend.iam.domain.model.PermissionInfo;
import com.zian.backend.iam.infrastructure.persistence.po.SysRolePermissionPO;
import com.zian.backend.iam.domain.model.Role;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色与角色-权限关联的管理 SQL。
 */
@Mapper
public interface RoleAdminMapper {

    /**
     * 查询全部启用角色。
     *
     * @return 角色（不含权限）
     */
    @Select("""
            SELECT id, role_code AS code, role_name AS name, data_scope AS dataScope
              FROM sys_role
             WHERE deleted = 0 AND enabled = 1
             ORDER BY id
            """)
    List<RoleRow> roles();

    /**
     * 查询单个角色的权限码。
     *
     * @param roleId 角色主键
     * @return 权限码
     */
    @Select("""
            SELECT p.perm_code
              FROM sys_role_permission rp
              JOIN sys_permission p ON p.id = rp.permission_id
             WHERE rp.role_id = #{roleId} AND p.deleted = 0 AND p.enabled = 1
             ORDER BY p.sort
            """)
    List<String> permissionCodes(@Param("roleId") long roleId);

    /**
     * 查询权限目录。
     *
     * @return 权限列表
     */
    @Select("""
            SELECT id, perm_code AS code, perm_name AS name, perm_type AS type,
                   SUBSTRING_INDEX(perm_code, ':', 1) AS module
              FROM sys_permission
             WHERE deleted = 0 AND enabled = 1
             ORDER BY sort, id
            """)
    List<PermissionInfo> permissions();

    /**
     * 清空角色的权限关联。
     *
     * @param roleId 角色主键
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void clear(@Param("roleId") long roleId);

    /**
     * 批量写入角色-权限关联。
     *
     * @param roleId 角色主键
     * @param ids 权限主键
     */
    @Insert("""
            <script>
            INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
            <foreach collection="rows" item="row" separator=",">
              (#{row.id}, #{row.roleId}, #{row.permissionId})
            </foreach>
            </script>
            """)
    void insertAll(@Param("rows") List<SysRolePermissionPO> rows);
}
