-- =============================================================
-- V6：读者消息中心
--   notify_delivery 此前没有「已读」状态，站内信只能投递不能标记阅读
--   /v1/notifications/** 是读者自助入口，新增 notify:read:self 权限
-- 注意：V4 起主键不再自增，本脚本必须显式提供 id（参考段固定 9e17 起，避开雪花区间）
-- 幂等：本脚本只执行一次（Flyway 版本化），INSERT 用 WHERE NOT EXISTS 兜底
-- =============================================================

ALTER TABLE notify_delivery
    ADD COLUMN read_at DATETIME NULL COMMENT '站内信阅读时间，NULL 表示未读';

INSERT INTO sys_permission (id, perm_code, perm_name, perm_type, path, method, sort)
SELECT 900000000000000510, 'notify:read:self', '读者消息中心', 'API', '/api/v1/notifications', 'GET', 7
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'notify:read:self' AND deleted = 0);

INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 900000000000000611
       + CASE r.role_code WHEN 'ADMIN' THEN 1 WHEN 'LIBRARIAN' THEN 2 ELSE 3 END,
       r.id, p.id
  FROM sys_role r
  JOIN sys_permission p
 WHERE r.role_code IN ('ADMIN', 'LIBRARIAN', 'READER')
   AND p.perm_code = 'notify:read:self'
   AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp
                    WHERE rp.role_id = r.id AND rp.permission_id = p.id);
