-- =============================================================
-- V2：运行期所需参考数据补齐
--   1) 罚金费率：schema.sql 的 26 个 rule_param 中没有费率，看板/归还结算需要按天计价
--   2) 工作台 / 通知管理权限码：管理端 UI 的 can() 用这些码做按钮级控制
-- 幂等：全部走 INSERT ... SELECT WHERE NOT EXISTS，重复执行不会产生脏数据
-- =============================================================

INSERT INTO rule_param (param_key, param_value, value_type, version, description)
SELECT 'FINE.PER_DAY', '0.50', 'DECIMAL', 'v1', '逾期罚金费率：元/天/册（仅 T4 及以后展示）'
 WHERE NOT EXISTS (SELECT 1 FROM rule_param WHERE param_key = 'FINE.PER_DAY' AND version = 'v1');

INSERT INTO sys_permission (perm_code, perm_name, perm_type, path, method, sort)
SELECT 'workbench:read', '工作台看板', 'API', '/api/v1/admin/dashboard', 'GET', 5
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'workbench:read' AND deleted = 0);

INSERT INTO sys_permission (perm_code, perm_name, perm_type, path, method, sort)
SELECT 'notify:manage', '通知模板与任务', 'API', '/api/v1/admin/templates', 'GET', 55
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'notify:manage' AND deleted = 0);

INSERT INTO sys_permission (perm_code, perm_name, perm_type, path, method, sort)
SELECT 'overdue:read', '逾期名单查看', 'API', '/api/v1/admin/overdue', 'GET', 61
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'overdue:read' AND deleted = 0);

-- 管理端权限码别名：与前端 src/lib/auth/permissions.ts 一一对应
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
  FROM sys_role r
  JOIN sys_permission p
 WHERE r.role_code IN ('ADMIN', 'LIBRARIAN')
   AND p.perm_code IN ('workbench:read', 'notify:manage', 'overdue:read')
   AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);
