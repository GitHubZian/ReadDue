package com.zian.backend.iam.infrastructure.persistence;

import com.zian.backend.iam.domain.model.UserRole;
import com.zian.backend.iam.domain.repository.PermissionRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 权限码仓储实现：角色 → 权限码（只读，走 sys_role_permission 关联表）。
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private static final String SQL = """
            SELECT p.perm_code
              FROM sys_role r
              JOIN sys_role_permission rp ON rp.role_id = r.id
              JOIN sys_permission p ON p.id = rp.permission_id
             WHERE r.role_code = ?
               AND r.deleted = 0
               AND r.enabled = 1
               AND p.deleted = 0
               AND p.enabled = 1
             ORDER BY p.sort
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<String> permissionsOf(UserRole role) {
        List<String> codes = jdbcTemplate.queryForList(SQL, String.class, role.name());
        return new LinkedHashSet<>(codes);
    }
}
