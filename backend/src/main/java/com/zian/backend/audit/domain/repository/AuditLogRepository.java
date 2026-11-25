package com.zian.backend.audit.domain.repository;

import com.zian.backend.audit.domain.model.AuditRecord;
import java.time.LocalDateTime;

/**
 * 审计日志仓储（只追加，按保留策略清理）。
 */
public interface AuditLogRepository {

    /**
     * 追加一条审计记录（objectId 为空时落 0）。
     */

    void append(AuditRecord record);

    /**
     * 删除早于截止时间的审计记录（保留期清理）。
     *
     * @param cutoff 截止时间（不含）
     * @return 删除行数
     */
    int deleteBefore(LocalDateTime cutoff);
}
