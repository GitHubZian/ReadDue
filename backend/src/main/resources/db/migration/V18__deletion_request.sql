-- M6：读者注销申请（先申请，管理员批准后复用「立即遗忘」脱敏与该读者的级联清理）。
-- 同一读者是否已有待处理申请由应用层查询保证；批准/驳回后保留记录供审计。
CREATE TABLE deletion_request (
    id          BIGINT UNSIGNED NOT NULL COMMENT '主键（IdWorker 生成）',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '申请读者',
    status      VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    reason      VARCHAR(255)    NULL COMMENT '读者填写的注销原因',
    note        VARCHAR(255)    NULL COMMENT '管理员处理备注',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_at  DATETIME        NULL COMMENT '处理时间',
    handled_by  BIGINT UNSIGNED NULL COMMENT '处理管理员',
    PRIMARY KEY (id),
    KEY idx_deletion_request_user (user_id, status),
    KEY idx_deletion_request_status (status, created_at),
    CONSTRAINT fk_deletion_request_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_deletion_request_handler FOREIGN KEY (handled_by) REFERENCES sys_user (id)
) ENGINE = InnoDB COMMENT = '读者注销申请（M6）';
