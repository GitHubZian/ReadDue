package com.zian.backend.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.reporting.application.AdminQueryService;
import com.zian.backend.reporting.interfaces.dto.AdminViews.AuditLogView;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * task-8：审计分类过滤与对象 ID 搜索。
 *
 * <p>{@code category} 是管理端页签分类（APPROVAL/RULE/USER/SYSTEM），不是动作前缀；
 * 过滤必须与列表里展示的 {@code category} 口径一致，且 count 与列表同源。
 */
@SpringBootTest
@Transactional
class AuditCategoryFilterTest {

    private static final int PAGE_SIZE = 200;

    @Autowired
    private AdminQueryService queryService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("category=APPROVAL 能查到 GRACE/APPEAL/RESERVATION，且不串到其它分类")
    void approvalCategoryCoversApprovalActions() {
        long marker = IdWorker.getId();
        insert(marker, "GRACE_APPROVE", "GRACE_APPLICATION");
        insert(marker, "APPEAL_RESOLVE", "APPEAL_TICKET");
        insert(marker, "RESERVATION_CONFIRM", "RESERVATION");
        insert(marker, "USER_FORGET", "SYS_USER");
        insert(marker, "RULE_UPDATE", "RULE_PARAM");

        PageResult<AuditLogView> approval =
                queryService.auditLogs("APPROVAL", String.valueOf(marker), PageQuery.of(1, PAGE_SIZE));
        assertThat(approval.total()).isEqualTo(3);
        assertThat(approval.items()).extracting(AuditLogView::action)
                .containsExactlyInAnyOrder("GRACE_APPROVE", "APPEAL_RESOLVE", "RESERVATION_CONFIRM");
        assertThat(approval.items()).allMatch(view -> "APPROVAL".equals(view.category()));

        PageResult<AuditLogView> rule =
                queryService.auditLogs("RULE", String.valueOf(marker), PageQuery.of(1, PAGE_SIZE));
        assertThat(rule.items()).extracting(AuditLogView::action).containsExactly("RULE_UPDATE");

        PageResult<AuditLogView> user =
                queryService.auditLogs("USER", String.valueOf(marker), PageQuery.of(1, PAGE_SIZE));
        assertThat(user.items()).extracting(AuditLogView::action).containsExactly("USER_FORGET");

        PageResult<AuditLogView> system =
                queryService.auditLogs("SYSTEM", String.valueOf(marker), PageQuery.of(1, PAGE_SIZE));
        assertThat(system.total()).isZero();
    }

    @Test
    @DisplayName("keyword 支持按对象 ID 搜索")
    void keywordMatchesObjectId() {
        long objectId = IdWorker.getId();
        insert(objectId, "GRACE_APPROVE", "GRACE_APPLICATION");

        PageResult<AuditLogView> page =
                queryService.auditLogs(null, String.valueOf(objectId), PageQuery.of(1, PAGE_SIZE));
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).singleElement().satisfies(view -> {
            assertThat(view.action()).isEqualTo("GRACE_APPROVE");
            assertThat(view.target()).contains(String.valueOf(objectId));
        });
    }

    private void insert(long objectId, String action, String objectType) {
        jdbc.update(
                "INSERT INTO audit_log (id, action, object_type, object_id, purpose, created_at) "
                        + "VALUES (?, ?, ?, ?, 'task-8 单测', NOW())",
                IdWorker.getId(),
                action,
                objectType,
                objectId);
    }
}
