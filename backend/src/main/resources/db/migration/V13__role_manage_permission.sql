-- 角色权限管理（B-12）：新增 role:manage 权限，仅授 ADMIN。

INSERT INTO sys_permission (id, perm_code, perm_name, perm_type, parent_id, path, method, sort, enabled)
VALUES (900000000000000540, 'role:manage', '角色权限管理', 'API', 0, '/api/v1/admin/roles', 'GET', 91, 1);

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 900000000000000641, r.id, 900000000000000540
FROM sys_role r
WHERE r.role_code = 'ADMIN';
