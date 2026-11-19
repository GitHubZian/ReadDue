package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.PermissionInfo;
import com.zian.backend.iam.domain.model.Role;
import com.zian.backend.iam.domain.repository.RoleRepository;
import com.zian.backend.iam.interfaces.dto.RoleDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色权限管理：查看角色、替换角色权限。
 *
 * <p>权限以数据库为准，JWT 过滤器每请求回源重算，因此改完立即生效；ADMIN 必须保留 `role:manage`，避免把自己锁在门外。
 */
@Service
@RequiredArgsConstructor
public class RoleAdminService {

    private static final String ADMIN = "ADMIN";
    private static final String ROLE_MANAGE = "role:manage";

    private final RoleRepository roleRepository;
    private final AuditRecorder auditRecorder;

    /**
     * 列出全部启用角色及权限。
     *
     * @return 角色视图列表
     */
    public List<RoleDtos.RoleView> roles() {
        return roleRepository.findAll().stream().map(RoleDtos.RoleView::of).toList();
    }

    /**
     * 权限目录。
     *
     * @return 权限视图列表
     */
    public List<RoleDtos.PermissionView> permissions() {
        return roleRepository.allPermissions().stream().map(RoleDtos.PermissionView::of).toList();
    }

    /**
     * 全量替换角色权限。
     *
     * @param code 角色编码
     * @param request 新的权限码集合
     * @return 替换后的角色权限
     * @throws BusinessException 角色或权限不存在（404）、ADMIN 移除角色管理权限（422）、权限码为空（400）
     */
    @Transactional
    public RoleDtos.UpdateResult updatePermissions(String code, RoleDtos.UpdatePermissionsRequest request) {
        Role role = roleRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在：" + code));
        List<String> requested = request == null ? null : request.permissions();
        if (requested == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "permissions 不能为空");
        }
        Map<String, PermissionInfo> catalog = new LinkedHashMap<>();
        roleRepository.allPermissions().forEach(info -> catalog.put(info.code(), info));
        LinkedHashSet<String> wanted = new LinkedHashSet<>();
        for (String raw : requested) {
            String permission = raw == null ? "" : raw.trim();
            if (permission.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限码不能为空");
            }
            if (!catalog.containsKey(permission)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "权限不存在：" + permission);
            }
            wanted.add(permission);
        }
        if (ADMIN.equals(role.code()) && !wanted.contains(ROLE_MANAGE)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "ADMIN 角色不能移除角色管理权限");
        }
        List<Long> ids = wanted.stream().map(code0 -> catalog.get(code0).id()).toList();
        roleRepository.replacePermissions(role.id(), ids);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("role", role.code());
        detail.put("permissionCount", ids.size());
        auditRecorder.record("ROLE_PERMISSION_UPDATE", "SYS_ROLE", role.id(), "调整角色权限", detail,
                Map.of("permissions", role.permissions()), Map.of("permissions", List.copyOf(wanted)));
        return new RoleDtos.UpdateResult(role.code(), List.copyOf(wanted));
    }
}
