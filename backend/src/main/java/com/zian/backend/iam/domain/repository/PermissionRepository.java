package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.UserRole;
import java.util.Set;

/**
 * 权限码仓储（角色 → 权限码，读多写少）。
 */
public interface PermissionRepository {

    /**
     * 查询角色的全部权限码。
     */

    Set<String> permissionsOf(UserRole role);
}
