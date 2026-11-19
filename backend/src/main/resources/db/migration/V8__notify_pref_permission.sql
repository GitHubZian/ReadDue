-- =============================================================
-- V8：读者渠道开关自助修改
--   GET /v1/me/channels 复用 notify:read:self（只读本人偏好）
--   PUT /v1/me/channels 需要独立写权限，避免读权限被当成写权限放行
-- 注意：V4 起主键不再自增，本脚本必须显式提供 id
-- =============================================================

INSERT INTO sys_permission (id, perm_code, perm_name, perm_type, path, method, sort)
SELECT 900000000000000520, 'notify:pref:self', '读者修改渠道偏好', 'API', '/api/v1/me/channels', 'PUT', 8
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'notify:pref:self' AND deleted = 0);

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 900000000000000621
       + CASE r.role_code WHEN 'ADMIN' THEN 1 WHEN 'LIBRARIAN' THEN 2 ELSE 3 END,
       r.id, p.id
  FROM sys_role r
  JOIN sys_permission p
 WHERE r.role_code IN ('ADMIN', 'LIBRARIAN', 'READER')
   AND p.perm_code = 'notify:pref:self'
   AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp
                    WHERE rp.role_id = r.id AND rp.permission_id = p.id);
