package com.zian.backend.audit.domain.repository;

import com.zian.backend.audit.domain.model.AuditRecord;

/**
 * 审计日志仓储（只追加）。
 */
public interface AuditLogRepository {

    /**
     * 追加一条审计记录（objectId 为空时落 0）。
     */

    void append(AuditRecord record);
}
