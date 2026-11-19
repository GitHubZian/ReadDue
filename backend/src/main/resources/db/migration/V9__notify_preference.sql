-- =============================================================
-- V9：FR-11 通知偏好
--   notify_topic          主题字典（优先级 P0/P1/P2 + 组织默认频率/渠道）
--   notify_preference     读者偏好（只存与默认不同的项）
--   notify_preference_log 偏好变更日志（before/after 供审计与读者自查）
--   notify_task 增加 topic_code / pref_snapshot，记录排程时的偏好快照
-- 注意：V4 起主键不再自增，种子数据必须显式提供 id
-- =============================================================

CREATE TABLE notify_topic (
    id               BIGINT UNSIGNED NOT NULL,
    code             VARCHAR(32)     NOT NULL COMMENT '主题编码，如 BORROW_DUE',
    name             VARCHAR(64)     NOT NULL COMMENT '展示名',
    priority         ENUM ('P0','P1','P2') NOT NULL COMMENT 'P0 不可关',
    default_frequency ENUM ('REALTIME','DAILY','WEEKLY') NOT NULL,
    default_channels JSON            NOT NULL COMMENT '默认渠道数组：INBOX/EMAIL',
    enabled          TINYINT         NOT NULL DEFAULT 1,
    sort             INT             NOT NULL DEFAULT 0,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_topic_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'FR-11 通知主题字典';

CREATE TABLE notify_preference (
    id         BIGINT UNSIGNED NOT NULL,
    user_id    BIGINT UNSIGNED NOT NULL,
    topic_id   BIGINT UNSIGNED NOT NULL,
    frequency  ENUM ('REALTIME','DAILY','WEEKLY') NOT NULL,
    channels   JSON            NOT NULL,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_topic (user_id, topic_id),
    CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_pref_topic FOREIGN KEY (topic_id) REFERENCES notify_topic (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'FR-11 读者通知偏好（只存差异）';

CREATE TABLE notify_preference_log (
    id          BIGINT UNSIGNED NOT NULL,
    user_id     BIGINT UNSIGNED NOT NULL,
    topic_code  VARCHAR(32)     NOT NULL,
    before_json JSON            NULL COMMENT '变更前偏好，首次设置为 NULL',
    after_json  JSON            NULL COMMENT '变更后偏好，恢复默认为 NULL',
    operator_id BIGINT UNSIGNED NULL COMMENT '操作者，读者自助时为本人',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_pref_log_user_time (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'FR-11 偏好变更日志';

ALTER TABLE notify_task
    ADD COLUMN topic_code    VARCHAR(32) NULL COMMENT '排程时归属的主题' AFTER level,
    ADD COLUMN pref_snapshot JSON        NULL COMMENT '排程时的偏好快照（频率/渠道/优先级）' AFTER quiet_shifted;

INSERT INTO notify_topic (id, code, name, priority, default_frequency, default_channels, sort)
VALUES (900000000000000810, 'BORROW_DUE', '到期提醒', 'P1', 'DAILY', '["INBOX","EMAIL"]', 10),
       (900000000000000811, 'BORROW_OVERDUE', '逾期提醒', 'P0', 'DAILY', '["INBOX","EMAIL"]', 20),
       (900000000000000812, 'RESERVATION_READY', '到书通知', 'P0', 'REALTIME', '["INBOX","EMAIL"]', 30),
       (900000000000000813, 'GRACE_RESULT', '宽限结果', 'P0', 'REALTIME', '["INBOX"]', 40),
       (900000000000000814, 'APPEAL_SLA', '申诉进度', 'P1', 'REALTIME', '["INBOX"]', 50),
       (900000000000000815, 'NOTICE_GENERAL', '一般通知', 'P2', 'DAILY', '["INBOX"]', 60);
