package com.zian.backend.iam.infrastructure.persistence.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色 → 权限码的只读查询（跨 sys_role / sys_role_permission / sys_permission 三表）。
 */
@Mapper
public interface PermissionQueryMapper {

    @Select("""
            SELECT p.perm_code
              FROM sys_role r
              JOIN sys_role_permission rp ON rp.role_id = r.id
              JOIN sys_permission p ON p.id = rp.permission_id
             WHERE r.role_code = #{role}
               AND r.deleted = 0
               AND r.enabled = 1
               AND p.deleted = 0
               AND p.enabled = 1
             ORDER BY p.sort
            """)
    List<String> codesOfRole(@Param("role") String role);
}
