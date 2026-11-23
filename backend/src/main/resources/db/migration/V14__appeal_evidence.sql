-- =============================================================
-- V14：B-09 申诉证据附件
--   appeal_evidence 申诉工单与附件的关联（一次申诉最多 5 个）
--   文件元数据复用 V10 的 file_object
-- 注意：V4 起主键不再自增，写入方必须显式提供 id
-- =============================================================

CREATE TABLE appeal_evidence (
    id         BIGINT UNSIGNED NOT NULL,
    appeal_id  BIGINT UNSIGNED NOT NULL,
    file_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_appeal_file (appeal_id, file_id),
    KEY idx_evidence_appeal (appeal_id),
    CONSTRAINT fk_appeal_evidence_appeal FOREIGN KEY (appeal_id) REFERENCES appeal_ticket (id),
    CONSTRAINT fk_appeal_evidence_file FOREIGN KEY (file_id) REFERENCES file_object (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '申诉工单证据附件（B-09）';
