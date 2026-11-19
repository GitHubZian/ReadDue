-- =============================================================
-- V7：读者同时在借上限
--   首页摘要（/v1/me/summary）需要返回 quota，借出接口按同一口径拦截超限。
--   主键不再自增（V4），显式给 id；幂等：已存在同键同版本则跳过。
-- =============================================================

INSERT INTO rule_param (id, param_key, param_value, value_type, version, description)
SELECT 900000000000000700, 'BORROW.MAX_ACTIVE', '5', 'INT', 'v1', '读者同时在借上限（册）'
 WHERE NOT EXISTS (
     SELECT 1 FROM rule_param WHERE param_key = 'BORROW.MAX_ACTIVE' AND version = 'v1'
 );
