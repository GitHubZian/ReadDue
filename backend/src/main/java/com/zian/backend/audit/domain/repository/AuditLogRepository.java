package com.zian.backend.audit.domain.repository;

import com.zian.backend.audit.domain.model.AuditRecord;

/**
 * 审计日志仓储（只追加）。
 */
public interface AuditLogRepository {

    void append(AuditRecord record);
}
