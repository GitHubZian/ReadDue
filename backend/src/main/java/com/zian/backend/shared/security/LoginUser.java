package com.zian.backend.shared.security;

import java.util.Set;

/**
 * 当前请求的登录主体（由 JWT 解析得到，不查库）。
 *
 * @param id          用户 id
 * @param userNo      学号 / 工号
 * @param name        姓名
 * @param role        ADMIN / LIBRARIAN / READER
 * @param permissions 权限码集合（来自 sys_role_permission，登录时快照进 token）
 */
public record LoginUser(Long id, String userNo, String name, String role, Set<String> permissions) {

    public boolean isStaff() {
        return "ADMIN".equals(role) || "LIBRARIAN".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * 判断当前登录用户是否拥有指定权限码。
     *
     * @param code 权限码
     * @return 拥有时为 true
     */
    public boolean hasPermission(String code) {
        return permissions != null && permissions.contains(code);
    }
}
