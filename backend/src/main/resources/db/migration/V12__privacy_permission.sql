-- 读者隐私自助：授权开关与「立即遗忘」（B-10）
-- 权限点只控制读者访问自己的隐私数据；ADMIN/LIBRARIAN 也同样持有，便于演示与联调。

INSERT INTO sys_permission (id, perm_code, perm_name, perm_type, parent_id, path, method, sort, enabled)
VALUES (900000000000000530, 'privacy:self', '隐私自助', 'API', 0, '/api/v1/me/consents', 'GET', 90, 1);

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 900000000000000631
        + CASE r.role_code WHEN 'ADMIN' THEN 1 WHEN 'LIBRARIAN' THEN 2 ELSE 3 END,
       r.id,
       900000000000000530
FROM sys_role r
WHERE r.role_code IN ('ADMIN', 'LIBRARIAN', 'READER');
