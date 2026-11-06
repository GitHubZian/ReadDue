package com.zian.backend.reporting.interfaces.dto;

import java.util.List;

/**
 * 管理端读模型（字段与前端 `src/lib/api/models.ts` 一一对应）。
 */
public final class AdminViews {

    private AdminViews() {
    }

    public record Person(String id, String name, String studentNo, String college) {
    }

    public record BookRef(String id, String title, String author, String barcode) {
    }

    public record TimelineItem(String at, String actor, String text) {
    }

    public record DashboardStats(
            int pendingGraces,
            int pendingAppeals,
            int dueToday,
            int overdueBooks,
            int overdueReaders,
            String overdueFine,
            int slaBreach,
            int pendingTotal) {
    }

    public record TrendPoint(String date, String label, int borrows, int returns) {
    }

    public record TrendSummary(int borrows, int returns) {
    }

    public record RuleHit(String label, int value) {
    }

    public record NoticeItem(String id, String level, String title, String body, String at) {
    }

    public record DueTodayItem(String id, Person reader, BookRef book, String dueAt, boolean reminded) {
    }

    public record Duty(String name, String window, String phone) {
    }

    public record DashboardData(
            DashboardStats stats,
            List<TrendPoint> trend,
            TrendSummary trendSummary,
            List<RuleHit> rulesHit,
            List<NoticeItem> notices,
            List<DueTodayItem> dueTodayItems,
            Duty duty) {
    }

    public record GraceRecordView(
            String id,
            String code,
            Person reader,
            BookRef book,
            String scene,
            String reason,
            String status,
            int requestedDays,
            Integer approvedDays,
            String slaDeadline,
            String createdAt,
            List<Evidence> evidence,
            List<TimelineItem> history) {
    }

    public record Evidence(String label, String url) {
    }

    public record AppealConclusion(String kind, Integer days, String note, String decidedAt, String decidedBy) {
    }

    public record AppealTicketView(
            String id,
            String code,
            Person reader,
            BookRef book,
            String category,
            String status,
            String summary,
            String createdAt,
            String slaDeadline,
            AppealConclusion conclusion,
            List<TimelineItem> timeline) {
    }

    public record ReservationView(
            String id,
            Person reader,
            BookRef book,
            String createdAt,
            int queuePosition,
            String status,
            String readyAt,
            String expiresAt,
            String shelf) {
    }

    public record OverdueItemView(
            String id,
            Person reader,
            BookRef book,
            String dueAt,
            int daysOverdue,
            String fine,
            int remindCount,
            String lastRemindedAt,
            String status) {
    }

    public record NotifyTaskView(
            String id,
            String title,
            String templateName,
            String channel,
            int targetCount,
            int successCount,
            int failCount,
            String status,
            String createdAt,
            String finishedAt,
            String error) {
    }

    public record AdminUserView(
            String id,
            String name,
            String studentNo,
            String role,
            String status,
            String college,
            String phone,
            String email,
            int borrowCount,
            String fine,
            String joinedAt,
            String lastActiveAt,
            String blacklistReason) {
    }

    public record AuditActor(String name, String role) {
    }

    public record AuditLogView(
            String id,
            String at,
            AuditActor actor,
            String action,
            String category,
            String target,
            String detail,
            String ip,
            String traceId,
            String before,
            String after) {
    }

    public record CirculationFlowView(
            String id,
            String at,
            String type,
            Person reader,
            BookRef book,
            String operator,
            String channel) {
    }

    public record CirculationTodayView(List<CirculationFlowView> flows, int borrows, int returns) {
    }

    public record RuleOption(String label, String value) {
    }

    public record RuleItemView(
            String key,
            String label,
            String description,
            String type,
            String value,
            String unit,
            List<RuleOption> options,
            Integer min,
            Integer max) {
    }

    public record RuleGroupView(String id, String title, String description, List<RuleItemView> items) {
    }

    public record RuleSetView(List<RuleGroupView> groups, String version) {
    }
}
