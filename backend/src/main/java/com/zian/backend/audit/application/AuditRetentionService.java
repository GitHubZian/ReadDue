package com.zian.backend.audit.application;

import com.zian.backend.audit.domain.repository.AuditLogRepository;
import com.zian.backend.rule.application.RuleService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审计日志保留策略：每天 03:30 清理 90 天前的记录。
 *
 * <p>只删除 {@code audit_log}，不触碰业务事实表；清理动作本身不再写审计，避免自引用循环。
 */
@Service
@RequiredArgsConstructor
public class AuditRetentionService {

    /** 审计保留天数。 */
    public static final int RETENTION_DAYS = 90;

    private static final Logger log = LoggerFactory.getLogger(AuditRetentionService.class);

    private final AuditLogRepository auditLogRepository;
    private final RuleService ruleService;

    /**
     * 清理过期审计日志（每天 03:30 执行）。
     *
     * @return 删除行数
     */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public int purge() {
        int days = ruleService.intValue("AUDIT.RETENTION_DAYS", RETENTION_DAYS);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = auditLogRepository.deleteBefore(cutoff);
        if (deleted > 0) {
            log.info("审计日志保留清理完成：删除 {} 条（早于 {}）", deleted, cutoff);
        }
        return deleted;
    }
}
