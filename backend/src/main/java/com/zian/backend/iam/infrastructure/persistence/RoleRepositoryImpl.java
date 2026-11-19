package com.zian.backend.iam.infrastructure.persistence;

import com.zian.backend.iam.domain.model.PermissionInfo;
import com.zian.backend.iam.domain.model.Role;
import com.zian.backend.iam.domain.repository.RoleRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.RoleRow;
import com.zian.backend.iam.infrastructure.persistence.po.SysRolePermissionPO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.iam.infrastructure.persistence.mapper.RoleAdminMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 角色管理仓储实现：角色列表、权限目录、角色-权限替换。
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleAdminMapper mapper;

    @Override
    public List<Role> findAll() {
        return mapper.roles().stream()
                .map(role -> new Role(role.id(), role.code(), role.name(), role.dataScope(),
                        mapper.permissionCodes(role.id())))
                .toList();
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return findAll().stream().filter(role -> role.code().equals(code)).findFirst();
    }

    @Override
    public List<PermissionInfo> allPermissions() {
        return mapper.permissions();
    }

    @Override
    public void replacePermissions(long roleId, List<Long> permissionIds) {
        mapper.clear(roleId);
        if (!permissionIds.isEmpty()) {
            List<SysRolePermissionPO> rows = permissionIds.stream().map(permissionId -> {
                SysRolePermissionPO row = new SysRolePermissionPO();
                row.setId(IdWorker.getId());
                row.setRoleId(roleId);
                row.setPermissionId(permissionId);
                row.setCreatedAt(LocalDateTime.now());
                return row;
            }).toList();
            mapper.insertAll(rows);
        }
    }
}
