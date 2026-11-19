-- B-09：审计可追溯（traceId + 变更前后值）
-- 三类写操作（宽限审批、申诉处理、规则调整）会写入 before_json/after_json；
-- 其余操作 trace_id 有值、前后值为 NULL。列可空，旧数据不受影响。
ALTER TABLE audit_log
    ADD COLUMN trace_id VARCHAR(64) NULL AFTER ip,
    ADD COLUMN before_json JSON NULL AFTER trace_id,
    ADD COLUMN after_json JSON NULL AFTER before_json,
    ADD KEY idx_trace (trace_id);
