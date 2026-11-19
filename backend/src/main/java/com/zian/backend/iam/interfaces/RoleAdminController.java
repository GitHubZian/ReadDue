package com.zian.backend.iam.interfaces;

import com.zian.backend.iam.application.RoleAdminService;
import com.zian.backend.iam.interfaces.dto.RoleDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端角色权限接口。
 *
 * <p>仅 ADMIN 可访问；替换权限后立即生效（权限以数据库为准）。
 */
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    /**
     * 角色列表（含权限码）。
     *
     * @return 角色视图列表
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role:manage')")
    public BaseResponse<List<RoleDtos.RoleView>> roles() {
        return ResultUtils.success(roleAdminService.roles());
    }

    /**
     * 权限目录（供角色授权勾选）。
     *
     * @return 权限视图列表
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role:manage')")
    public BaseResponse<List<RoleDtos.PermissionView>> permissions() {
        return ResultUtils.success(roleAdminService.permissions());
    }

    /**
     * 全量替换角色权限。
     *
     * @param code 角色编码
     * @param request 新的权限码集合
     * @return 替换后的角色权限
     */
    @PutMapping("/roles/{code}/permissions")
    @PreAuthorize("hasAuthority('role:manage')")
    public BaseResponse<RoleDtos.UpdateResult> updatePermissions(@PathVariable String code,
                                                                 @RequestBody RoleDtos.UpdatePermissionsRequest request) {
        return ResultUtils.success(roleAdminService.updatePermissions(code, request));
    }
}
