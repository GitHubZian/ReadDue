-- =============================================================
-- V3：并发兜底约束（对应安全审计 M-5）
--   1) 同一副本同时只能有一条在借单
--   2) 同一读者同一书目同时只能有一条在借单
--   3) 同一读者同一书目同时只能有一条有效预约（WAITING / READY）
--
-- 用「结束态为 NULL 的生成列 + 唯一索引」表达部分唯一：
-- 唯一索引允许多个 NULL，因此历史单（RETURNED / LOST / CLOSED 等）互不冲突，
-- 而并发的「先查后插」会在数据库层被拒绝，由应用转成 409 提示刷新。
-- =============================================================

ALTER TABLE borrow_order
    ADD COLUMN active_copy_id BIGINT
        GENERATED ALWAYS AS (CASE WHEN status IN ('BORROWED', 'OVERDUE') THEN copy_id ELSE NULL END) STORED,
    ADD COLUMN active_reader_book VARCHAR(64)
        GENERATED ALWAYS AS (
            CASE WHEN status IN ('BORROWED', 'OVERDUE') THEN CONCAT(reader_id, '-', book_id) ELSE NULL END
        ) STORED,
    ADD UNIQUE KEY uk_borrow_active_copy (active_copy_id),
    ADD UNIQUE KEY uk_borrow_active_reader_book (active_reader_book);

ALTER TABLE reservation
    ADD COLUMN active_reader_book VARCHAR(64)
        GENERATED ALWAYS AS (
            CASE WHEN status IN ('WAITING', 'READY') THEN CONCAT(reader_id, '-', book_id) ELSE NULL END
        ) STORED,
    ADD UNIQUE KEY uk_reservation_active_reader_book (active_reader_book);
