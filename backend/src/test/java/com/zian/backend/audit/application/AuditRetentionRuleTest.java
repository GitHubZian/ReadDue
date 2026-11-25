package com.zian.backend.audit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zian.backend.audit.domain.repository.AuditLogRepository;
import com.zian.backend.rule.application.RuleService;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * 保留天数来自规则 AUDIT.RETENTION_DAYS，缺省 90 天。
 */
class AuditRetentionRuleTest {

    @Test
    void purgeUsesConfiguredRetentionDays() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        RuleService ruleService = mock(RuleService.class);
        when(ruleService.intValue("AUDIT.RETENTION_DAYS", AuditRetentionService.RETENTION_DAYS))
                .thenReturn(1);
        when(repository.deleteBefore(any())).thenReturn(3);

        AuditRetentionService service = new AuditRetentionService(repository, ruleService);
        assertThat(service.purge()).isEqualTo(3);

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).deleteBefore(cutoff.capture());
        assertThat(Duration.between(cutoff.getValue(), LocalDateTime.now()).toHours()).isEqualTo(24);
    }
}
