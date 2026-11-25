package com.zian.backend.audit.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.security.SecurityTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * M5-C：审计日志 90 天保留策略。
 */
class AuditRetentionTest extends SecurityTestSupport {

    @Autowired
    private AuditRetentionService retentionService;

    @Test
    void purgeDeletesOnlyExpiredRows() {
        long oldId = IdWorker.getId();
        long freshId = IdWorker.getId();
        insertAudit(oldId, "RETENTION_OLD", "DATE_SUB(NOW(), INTERVAL 91 DAY)");
        insertAudit(freshId, "RETENTION_FRESH", "NOW()");

        int removed = retentionService.purge();

        assertTrue(removed >= 1, "至少删除 1 条 90 天前的日志");
        assertEquals(
                0,
                jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE id = ?", Integer.class, oldId));
        assertEquals(
                1,
                jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE id = ?", Integer.class, freshId));
    }

    private void insertAudit(long id, String action, String createdAtExpression) {
        jdbc.update(
                """
                INSERT INTO audit_log (id, actor_role, action, object_type, object_id, purpose, created_at)
                VALUES (?, 'ADMIN', ?, 'SYS_USER', 1, '保留策略测试', %s)
                """
                        .formatted(createdAtExpression),
                id,
                action);
    }
}
