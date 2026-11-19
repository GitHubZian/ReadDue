-- =============================================================
-- V10：B-08 宽限证据附件
--   file_object    上传文件元数据（磁盘只存路径，缩略/鉴权在应用层）
--   grace_evidence 宽限申请与附件的关联（一次申请最多 5 个）
-- 注意：V4 起主键不再自增，写入方必须显式提供 id
-- =============================================================

CREATE TABLE file_object (
    id            BIGINT UNSIGNED NOT NULL,
    owner_id      BIGINT UNSIGNED NOT NULL COMMENT '上传者',
    original_name VARCHAR(200)    NOT NULL COMMENT '原始文件名（已去路径与控制字符）',
    content_type  VARCHAR(64)     NOT NULL COMMENT 'image/png、image/jpeg、application/pdf',
    size_bytes    BIGINT          NOT NULL,
    sha256        CHAR(64)        NOT NULL COMMENT '内容摘要，用于去重/取证',
    storage_path  VARCHAR(255)    NOT NULL COMMENT '相对存储根目录的路径，如 2026/11/123.png',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_file_owner (owner_id, created_at),
    CONSTRAINT fk_file_owner FOREIGN KEY (owner_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '上传文件元数据（B-08）';

CREATE TABLE grace_evidence (
    id         BIGINT UNSIGNED NOT NULL,
    grace_id   BIGINT UNSIGNED NOT NULL,
    file_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_grace_file (grace_id, file_id),
    KEY idx_evidence_grace (grace_id),
    CONSTRAINT fk_evidence_grace FOREIGN KEY (grace_id) REFERENCES grace_application (id),
    CONSTRAINT fk_evidence_file FOREIGN KEY (file_id) REFERENCES file_object (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '宽限申请证据附件（B-08）';
