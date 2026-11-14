package com.zian.backend.iam.infrastructure.persistence;

import com.zian.backend.iam.domain.model.UserRole;
import com.zian.backend.iam.domain.repository.PermissionRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.PermissionQueryMapper;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 权限码仓储实现：角色 → 权限码（只读，走 sys_role_permission 关联表）。
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionQueryMapper queryMapper;

    @Override
    public Set<String> permissionsOf(UserRole role) {
        return new LinkedHashSet<>(queryMapper.codesOfRole(role.name()));
    }
}
