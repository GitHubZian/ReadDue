-- =============================================================
-- V5：读者端书目检索权限
--   /v1/books/** 是读者、馆员、管理员共用的只读检索入口，此前没有对应权限码
-- 注意：V4 起主键不再自增，本脚本必须显式提供 id（参考段固定 9e17 起，避开雪花区间）
-- 幂等：全部走 INSERT ... SELECT WHERE NOT EXISTS
-- =============================================================

INSERT INTO sys_permission (id, perm_code, perm_name, perm_type, path, method, sort)
SELECT 900000000000000500, 'catalog:read', '书目检索与馆藏', 'API', '/api/v1/books', 'GET', 6
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'catalog:read' AND deleted = 0);

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 900000000000000601
       + CASE r.role_code WHEN 'ADMIN' THEN 1 WHEN 'LIBRARIAN' THEN 2 ELSE 3 END,
       r.id, p.id
  FROM sys_role r
  JOIN sys_permission p
 WHERE r.role_code IN ('ADMIN', 'LIBRARIAN', 'READER')
   AND p.perm_code = 'catalog:read'
   AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp
                    WHERE rp.role_id = r.id AND rp.permission_id = p.id);
