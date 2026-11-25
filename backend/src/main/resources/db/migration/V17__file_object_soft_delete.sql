-- M5-B：遗忘级联删除证据文件时保留元数据行，仅逻辑删除，避免审计断链。
-- deleted=1 的文件不再可下载，也不再作为有效证据展示；物理内容由应用删除。
ALTER TABLE file_object
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0 有效；1 已随遗忘/清理删除' AFTER storage_path,
    ADD KEY idx_file_object_deleted (deleted);
