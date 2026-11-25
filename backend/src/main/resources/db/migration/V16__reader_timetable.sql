-- M5-F：读者课表（隐私「场景声明」数据源；关闭场景授权时不读取）。
-- 一个读者可维护多门课程，按星期 + 起止时间保存；PUT 覆盖式更新，旧记录逻辑删除保留审计痕迹。
DROP TABLE IF EXISTS reader_timetable;
CREATE TABLE reader_timetable (
    id          BIGINT UNSIGNED NOT NULL COMMENT '主键（IdWorker 生成）',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '读者账号',
    weekday     TINYINT         NOT NULL COMMENT '星期：1=周一 … 7=周日',
    start_time  TIME            NOT NULL COMMENT '开始时间',
    end_time    TIME            NOT NULL COMMENT '结束时间',
    deleted     TINYINT         NOT NULL DEFAULT 0 COMMENT '0 有效；1 已被覆盖更新',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_reader_timetable_user (user_id, deleted),
    CONSTRAINT fk_reader_timetable_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB COMMENT = '读者课表（场景声明，M5-F）';
