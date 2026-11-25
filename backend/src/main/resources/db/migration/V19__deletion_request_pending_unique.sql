-- 同一读者同时只允许一条 PENDING 注销申请（由数据库兜底并发重复提交）。
ALTER TABLE deletion_request
    ADD COLUMN pending_user_id BIGINT UNSIGNED
        GENERATED ALWAYS AS (IF(status = 'PENDING', user_id, NULL)) STORED
        COMMENT '仅待处理申请有值，用于唯一约束',
    ADD UNIQUE KEY uk_deletion_request_pending_user (pending_user_id);
