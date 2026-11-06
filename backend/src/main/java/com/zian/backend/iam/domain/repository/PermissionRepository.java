package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.UserRole;
import java.util.Set;

/**
 * 权限码仓储（角色 → 权限码，读多写少）。
 */
public interface PermissionRepository {

    Set<String> permissionsOf(UserRole role);
}
