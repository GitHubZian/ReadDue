package com.zian.backend.reporting.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.notify.application.NotifyTemplateService;
import com.zian.backend.notify.domain.repository.NotifyTemplateRepository;
import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.reporting.interfaces.dto.AdminViews;
import com.zian.backend.reporting.interfaces.dto.AdminViews.AdminUserView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.AppealConclusion;
import com.zian.backend.reporting.interfaces.dto.AdminViews.AppealTicketView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.AuditActor;
import com.zian.backend.reporting.interfaces.dto.AdminViews.AuditLogView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.BookRef;
import com.zian.backend.reporting.interfaces.dto.AdminViews.CirculationFlowView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.CirculationTodayView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.DashboardData;
import com.zian.backend.reporting.interfaces.dto.AdminViews.DashboardStats;
import com.zian.backend.reporting.interfaces.dto.AdminViews.DueTodayItem;
import com.zian.backend.reporting.interfaces.dto.AdminViews.Duty;
import com.zian.backend.reporting.interfaces.dto.AdminViews.GraceRecordView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.NoticeItem;
import com.zian.backend.reporting.interfaces.dto.AdminViews.NotifyTaskView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.OverdueItemView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.Person;
import com.zian.backend.reporting.interfaces.dto.AdminViews.ReservationView;
import com.zian.backend.reporting.interfaces.dto.AdminViews.RuleHit;
import com.zian.backend.reporting.interfaces.dto.AdminViews.TimelineItem;
import com.zian.backend.reporting.interfaces.dto.AdminViews.TrendPoint;
import com.zian.backend.reporting.interfaces.dto.AdminViews.TrendSummary;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * 管理端只读查询服务：跨上下文聚合全部走 SQL，不触碰任何上下文的 Mapper。
 */
@Service
@RequiredArgsConstructor
public class AdminQueryService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM-dd");

    private final JdbcTemplate jdbc;
    private final RuleService ruleService;
    private final NotifyTemplateRepository templateRepository;
    private final NotifyTemplateService templateService;
    private final ObjectMapper objectMapper;

    // ------------------------------------------------------------ 工作台

    public DashboardData dashboard() {
        int pendingGraces = count("SELECT COUNT(*) FROM grace_application WHERE status = 'PENDING'");
        int pendingAppeals = count("SELECT COUNT(*) FROM appeal_ticket WHERE status IN ('OPEN','ACKED')");
        int dueToday = count(
                "SELECT COUNT(*) FROM borrow_order WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at = CURDATE()");
        int overdueBooks = count(
                "SELECT COUNT(*) FROM borrow_order WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at < CURDATE()");
        int overdueReaders = count(
                "SELECT COUNT(DISTINCT reader_id) FROM borrow_order WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at < CURDATE()");
        int slaBreach = count(
                "SELECT COUNT(*) FROM appeal_ticket WHERE status IN ('OPEN','ACKED') AND sla_breached = 0 AND sla_due_at < NOW()");
        BigDecimal fine = sumOverdueFine();
        DashboardStats stats = new DashboardStats(
                pendingGraces,
                pendingAppeals,
                dueToday,
                overdueBooks,
                overdueReaders,
                fine.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                slaBreach,
                pendingGraces + pendingAppeals);

        List<TrendPoint> trend = trend();
        int borrowSum = trend.stream().mapToInt(TrendPoint::borrows).sum();
        int returnSum = trend.stream().mapToInt(TrendPoint::returns).sum();

        List<RuleHit> hits = List.of(
                new RuleHit("宽限封顶（天）", ruleService.intValue("GRACE.DMAX", 14)),
                new RuleHit("续借上限（次）", ruleService.intValue("RENEW.MAX_TIMES", 1)),
                new RuleHit("SLA 时限（小时）", ruleService.intValue("APPEAL.SLA_HOURS", 2)));

        return new DashboardData(
                stats,
                trend,
                new TrendSummary(borrowSum, returnSum),
                hits,
                notices(),
                dueTodayItems(),
                new Duty("值班馆员 · 王倩", "08:00–22:00", "内线 8021"));
    }

    private List<TrendPoint> trend() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);
        Map<LocalDate, int[]> buckets = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            buckets.put(from.plusDays(i), new int[] {0, 0});
        }
        jdbc.query(
                "SELECT DATE(borrowed_at) d, COUNT(*) c FROM borrow_order WHERE borrowed_at >= ? GROUP BY DATE(borrowed_at)",
                rs -> {
                    LocalDate d = rs.getDate(1).toLocalDate();
                    if (buckets.containsKey(d)) {
                        buckets.get(d)[0] = rs.getInt(2);
                    }
                },
                from.atStartOfDay());
        jdbc.query(
                "SELECT DATE(returned_at) d, COUNT(*) c FROM borrow_order WHERE returned_at >= ? GROUP BY DATE(returned_at)",
                rs -> {
                    LocalDate d = rs.getDate(1).toLocalDate();
                    if (buckets.containsKey(d)) {
                        buckets.get(d)[1] = rs.getInt(2);
                    }
                },
                from.atStartOfDay());
        List<TrendPoint> points = new ArrayList<>();
        buckets.forEach((day, counts) -> points.add(
                new TrendPoint(day.toString(), day.format(DAY), counts[0], counts[1])));
        return points;
    }

    private List<NoticeItem> notices() {
        return jdbc.query(
                """
                SELECT t.id, t.level, t.plan_at, u.name AS reader_name
                FROM notify_task t
                LEFT JOIN sys_user u ON u.id = t.reader_id
                WHERE t.status = 'SENT'
                ORDER BY t.plan_at DESC
                LIMIT 5
                """,
                (rs, i) -> new NoticeItem(
                        String.valueOf(rs.getLong("id")),
                        tone(rs.getString("level")),
                        levelName(rs.getString("level")),
                        (rs.getString("reader_name") == null ? "读者" : rs.getString("reader_name")) + " · 已送达站内信",
                        iso(rs.getTimestamp("plan_at"))));
    }

    private List<DueTodayItem> dueTodayItems() {
        return jdbc.query(
                """
                SELECT o.id, u.name AS reader_name, u.user_no, rp.department,
                       b.id AS book_id, b.title, b.author, c.barcode, o.effective_due_at
                FROM borrow_order o
                JOIN sys_user u ON u.id = o.reader_id
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                JOIN book b ON b.id = o.book_id
                LEFT JOIN book_copy c ON c.id = o.copy_id
                WHERE o.status IN ('BORROWED','OVERDUE') AND o.effective_due_at = CURDATE()
                ORDER BY o.effective_due_at
                LIMIT 8
                """,
                (rs, i) -> new DueTodayItem(
                        String.valueOf(rs.getLong("id")),
                        new Person(
                                String.valueOf(rs.getLong("id")),
                                rs.getString("reader_name"),
                                rs.getString("user_no"),
                                rs.getString("department")),
                        new BookRef(
                                String.valueOf(rs.getLong("book_id")),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("barcode")),
                        rs.getDate("effective_due_at").toString(),
                        false));
    }

    // ------------------------------------------------------------ 宽限

    public PageResult<GraceRecordView> graces(String status, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        String where = graceWhere(status, keyword, params);
        String base = """
                FROM grace_application g
                JOIN sys_user u ON u.id = g.reader_id
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                JOIN borrow_order o ON o.id = g.borrow_order_id
                JOIN book b ON b.id = o.book_id
                LEFT JOIN book_copy c ON c.id = o.copy_id
                """ + where;
        String select = """
                SELECT g.id, g.apply_no, g.scene_code, g.evidence_text, g.status, g.computed_days,
                       g.granted_days, g.created_at, g.decided_at, g.decided_by, g.reject_code,
                       u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                       b.id AS book_id, b.title, b.author, c.barcode
                """ + base;
        return page(select + " ORDER BY g.created_at DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> {
            String dbStatus = rs.getString("status");
            Integer approved = rs.getObject("granted_days") == null ? null : rs.getInt("granted_days");
            List<TimelineItem> history = new ArrayList<>();
            history.add(new TimelineItem(
                    iso(rs.getTimestamp("created_at")),
                    rs.getString("reader_name"),
                    "提交宽限申请 · 场景 " + rs.getString("scene_code")));
            if (rs.getTimestamp("decided_at") != null) {
                history.add(new TimelineItem(
                        iso(rs.getTimestamp("decided_at")),
                        rs.getString("decided_by") == null ? "馆员" : String.valueOf(rs.getLong("decided_by")),
                        ("APPROVED".equals(dbStatus) ? "通过" : "驳回") + (approved == null ? "" : "，宽限 " + approved + " 天")));
            }
            return new GraceRecordView(
                    String.valueOf(rs.getLong("id")),
                    rs.getString("apply_no"),
                    new Person(
                            String.valueOf(rs.getLong("reader_id")),
                            rs.getString("reader_name"),
                            rs.getString("user_no"),
                            rs.getString("department")),
                    new BookRef(
                            String.valueOf(rs.getLong("book_id")),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("barcode")),
                    rs.getString("scene_code"),
                    rs.getString("evidence_text"),
                    graceStatus(dbStatus),
                    (int) Math.ceil(rs.getBigDecimal("computed_days").doubleValue()),
                    approved,
                    iso(rs.getTimestamp("created_at")) == null
                            ? null
                            : isoPlusHours(rs.getTimestamp("created_at"), ruleService.intValue("APPEAL.SLA_HOURS", 2)),
                    iso(rs.getTimestamp("created_at")),
                    List.of(),
                    history);
        });
    }

    private String graceWhere(String status, String keyword, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            switch (status) {
                case "PENDING" -> where.append(" AND g.status IN ('PENDING','APPEALING')");
                case "APPROVED" -> where.append(" AND g.status = 'APPROVED'");
                case "REJECTED" -> where.append(" AND g.status = 'REJECTED'");
                case "EXPIRED" -> where.append(" AND g.status IN ('EXPIRED','CLOSED')");
                default -> {
                }
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.name LIKE ? OR u.user_no LIKE ? OR b.title LIKE ? OR g.apply_no LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        return where.toString();
    }

    private String graceStatus(String db) {
        return switch (db) {
            case "APPROVED" -> "APPROVED";
            case "REJECTED" -> "REJECTED";
            case "EXPIRED" -> "EXPIRED";
            default -> "PENDING";
        };
    }

    // ------------------------------------------------------------ 申诉

    public PageResult<AppealTicketView> appeals(String status, String category, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            switch (status) {
                case "PENDING" -> where.append(" AND a.status = 'OPEN'");
                case "PROCESSING" -> where.append(" AND a.status = 'ACKED'");
                case "RESOLVED" -> where.append(" AND a.status IN ('RESOLVED','AUTO_COMPENSATED','CLOSED')");
                case "REJECTED" -> where.append(" AND a.status = 'REJECTED'");
                default -> {
                }
            }
        }
        if (category != null && !category.isBlank() && !"ALL".equals(category)) {
            switch (category) {
                case "OVERDUE_DISPUTE" -> where.append(" AND a.target_type = 'OVERDUE_TIER'");
                case "FINE_DISPUTE" -> where.append(" AND a.target_type = 'GRACE'");
                default -> where.append(" AND a.target_type NOT IN ('OVERDUE_TIER','GRACE')");
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.name LIKE ? OR u.user_no LIKE ? OR b.title LIKE ? OR a.ticket_no LIKE ? OR a.reason LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(like);
            }
        }
        String base = """
                FROM appeal_ticket a
                JOIN sys_user u ON u.id = a.reader_id
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                LEFT JOIN borrow_order o ON o.id = CASE
                     WHEN a.target_type = 'OVERDUE_TIER' THEN a.target_id
                     WHEN a.target_type = 'GRACE' THEN (SELECT ga.borrow_order_id FROM grace_application ga WHERE ga.id = a.target_id)
                     ELSE NULL END
                LEFT JOIN book b ON b.id = o.book_id
                LEFT JOIN book_copy c ON c.id = o.copy_id
                """ + where;
        String select = """
                SELECT a.id, a.ticket_no, a.target_type, a.reason, a.status, a.created_at, a.sla_due_at,
                       a.first_reply_at, a.resolution, a.compensation_days, a.assignee_name,
                       u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                       b.id AS book_id, b.title, b.author, c.barcode
                """ + base;
        return page(select + " ORDER BY a.created_at DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> {
            String dbStatus = rs.getString("status");
            List<TimelineItem> timeline = new ArrayList<>();
            timeline.add(new TimelineItem(
                    iso(rs.getTimestamp("created_at")), rs.getString("reader_name"), "提交申诉：" + rs.getString("reason")));
            if (rs.getTimestamp("first_reply_at") != null) {
                timeline.add(new TimelineItem(
                        iso(rs.getTimestamp("first_reply_at")),
                        rs.getString("assignee_name") == null ? "馆员" : rs.getString("assignee_name"),
                        "受理工单，进入核查"));
            }
            AppealConclusion conclusion = null;
            if (!"OPEN".equals(dbStatus) && !"ACKED".equals(dbStatus)) {
                String kind = switch (dbStatus) {
                    case "REJECTED" -> "REJECT";
                    case "AUTO_COMPENSATED" -> "COMPENSATE";
                    default -> "REVOKE_OVERDUE";
                };
                conclusion = new AppealConclusion(
                        kind,
                        rs.getObject("compensation_days") == null ? null : rs.getInt("compensation_days"),
                        rs.getString("resolution") == null ? "" : rs.getString("resolution"),
                        iso(rs.getTimestamp("created_at")),
                        rs.getString("assignee_name") == null ? "系统" : rs.getString("assignee_name"));
                timeline.add(new TimelineItem(
                        iso(rs.getTimestamp("created_at")),
                        rs.getString("assignee_name") == null ? "系统" : rs.getString("assignee_name"),
                        "结论：" + conclusion.note()));
            }
            return new AppealTicketView(
                    String.valueOf(rs.getLong("id")),
                    rs.getString("ticket_no"),
                    new Person(
                            String.valueOf(rs.getLong("reader_id")),
                            rs.getString("reader_name"),
                            rs.getString("user_no"),
                            rs.getString("department")),
                    new BookRef(
                            rs.getObject("book_id") == null ? null : String.valueOf(rs.getLong("book_id")),
                            rs.getString("title") == null ? "关联借阅单" : rs.getString("title"),
                            rs.getString("author") == null ? "" : rs.getString("author"),
                            rs.getString("barcode")),
                    appealCategory(rs.getString("target_type")),
                    appealStatus(dbStatus),
                    rs.getString("reason"),
                    iso(rs.getTimestamp("created_at")),
                    iso(rs.getTimestamp("sla_due_at")),
                    conclusion,
                    timeline);
        });
    }

    private String appealStatus(String db) {
        return switch (db) {
            case "OPEN" -> "PENDING";
            case "ACKED" -> "PROCESSING";
            case "REJECTED" -> "REJECTED";
            default -> "RESOLVED";
        };
    }

    private String appealCategory(String targetType) {
        return switch (targetType) {
            case "OVERDUE_TIER" -> "OVERDUE_DISPUTE";
            case "GRACE" -> "FINE_DISPUTE";
            case "OTHER" -> "BLACKLIST";
            default -> "OTHER";
        };
    }

    // ------------------------------------------------------------ 预约

    public PageResult<ReservationView> reservations(String status, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            where.append(" AND r.status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.name LIKE ? OR u.user_no LIKE ? OR b.title LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String base = """
                FROM reservation r
                JOIN sys_user u ON u.id = r.reader_id
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                JOIN book b ON b.id = r.book_id
                """ + where;
        String select = """
                SELECT r.id, r.queue_no, r.status, r.created_at, r.ready_at, r.expires_at,
                       u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                       b.id AS book_id, b.title, b.author
                """ + base;
        return page(select + " ORDER BY r.created_at DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> new ReservationView(
                String.valueOf(rs.getLong("id")),
                new Person(
                        String.valueOf(rs.getLong("reader_id")),
                        rs.getString("reader_name"),
                        rs.getString("user_no"),
                        rs.getString("department")),
                new BookRef(
                        String.valueOf(rs.getLong("book_id")), rs.getString("title"), rs.getString("author"), null),
                iso(rs.getTimestamp("created_at")),
                rs.getInt("queue_no"),
                rs.getString("status"),
                iso(rs.getTimestamp("ready_at")),
                iso(rs.getTimestamp("expires_at")),
                "READY".equals(rs.getString("status")) ? "预约架 A-02" : null));
    }

    // ------------------------------------------------------------ 逾期

    public PageResult<OverdueItemView> overdue(String status, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE v.effective_due_at < CURDATE()");
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            if ("LOST".equals(status)) {
                where.append(" AND o.status = 'LOST'");
            } else {
                where.append(" AND o.status IN ('BORROWED','OVERDUE')");
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (v.reader_name LIKE ? OR v.student_no LIKE ? OR v.book_title LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String base = """
                FROM v_overdue_board v
                JOIN borrow_order o ON o.id = v.order_id
                LEFT JOIN book_copy c ON c.id = o.copy_id
                """ + where;
        String select = """
                SELECT v.order_id, v.reader_id, v.reader_name, v.student_no, v.book_id, v.book_title,
                       v.effective_due_at, v.overdue_days, v.l3_sent, o.status,
                       (SELECT MAX(t.created_at) FROM notify_task t
                         WHERE t.borrow_order_id = v.order_id AND t.level = 'L3') AS last_reminded,
                       (SELECT p.department FROM reader_profile p WHERE p.user_id = v.reader_id) AS department,
                       c.barcode, (SELECT b.author FROM book b WHERE b.id = v.book_id) AS author
                """ + base;
        BigDecimal rate = ruleService.decimal("FINE.PER_DAY", "0.50");
        return page(select + " ORDER BY v.overdue_days DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> {
            int days = Math.max(rs.getInt("overdue_days"), 0);
            return new OverdueItemView(
                    String.valueOf(rs.getLong("order_id")),
                    new Person(
                            String.valueOf(rs.getLong("reader_id")),
                            rs.getString("reader_name"),
                            rs.getString("student_no"),
                            rs.getString("department")),
                    new BookRef(
                            String.valueOf(rs.getLong("book_id")),
                            rs.getString("book_title"),
                            rs.getString("author"),
                            rs.getString("barcode")),
                    rs.getDate("effective_due_at").toString(),
                    days,
                    rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    rs.getInt("l3_sent"),
                    iso(rs.getTimestamp("last_reminded")),
                    "LOST".equals(rs.getString("status")) ? "LOST" : "OVERDUE");
        });
    }

    // ------------------------------------------------------------ 提醒

    public List<NotifyDtos.TemplateView> templates() {
        return templateRepository.findAll().stream().map(templateService::view).toList();
    }

    public PageResult<NotifyTaskView> notifyTasks(String status, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            String mapped = switch (status) {
                case "PENDING" -> "PLANNED";
                case "SENDING" -> "QUEUED";
                default -> status;
            };
            where.append(" AND t.status = ?");
            params.add(mapped);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.name LIKE ? OR t.dedup_key LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        String base = """
                FROM notify_task t
                LEFT JOIN sys_user u ON u.id = t.reader_id
                """ + where;
        String select = """
                SELECT t.id, t.level, t.status, t.created_at, t.plan_at, t.updated_at, t.dedup_key,
                       u.name AS reader_name
                """ + base;
        return page(select + " ORDER BY t.created_at DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> {
            String dbStatus = rs.getString("status");
            String statusView = switch (dbStatus) {
                case "PLANNED", "CANCELED", "MERGED" -> "PENDING";
                case "QUEUED" -> "SENDING";
                default -> dbStatus;
            };
            boolean sent = "SENT".equals(dbStatus);
            boolean failed = "FAILED".equals(dbStatus);
            return new NotifyTaskView(
                    String.valueOf(rs.getLong("id")),
                    levelName(rs.getString("level")) + " · " + (rs.getString("reader_name") == null ? "读者" : rs.getString("reader_name")),
                    levelName(rs.getString("level")),
                    "INAPP",
                    1,
                    sent ? 1 : 0,
                    failed ? 1 : 0,
                    statusView,
                    iso(rs.getTimestamp("created_at")),
                    sent ? iso(rs.getTimestamp("updated_at")) : null,
                    failed ? "投递失败，可重试" : null);
        });
    }

    // ------------------------------------------------------------ 用户

    public PageResult<AdminUserView> users(String role, String status, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE u.deleted = 0");
        if (role != null && !role.isBlank() && !"ALL".equals(role)) {
            where.append(" AND u.role = ?");
            params.add(role);
        }
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            where.append(" AND u.status = ?");
            params.add("ACTIVE".equals(status) ? "NORMAL" : status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.name LIKE ? OR u.user_no LIKE ? OR u.phone LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String base = """
                FROM sys_user u
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                """ + where;
        String select = """
                SELECT u.id, u.name, u.user_no, u.role, u.status, u.phone, u.email, u.created_at, u.last_login_at,
                       u.blacklist_reason, rp.department,
                       (SELECT COUNT(*) FROM borrow_order o WHERE o.reader_id = u.id) AS borrow_count,
                       (SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), o.effective_due_at), 0)), 0)
                          FROM borrow_order o
                         WHERE o.reader_id = u.id AND o.status IN ('BORROWED','OVERDUE')) AS overdue_days
                """ + base;
        BigDecimal rate = ruleService.decimal("FINE.PER_DAY", "0.50");
        return page(select + " ORDER BY u.created_at DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> {
            int overdueDays = rs.getInt("overdue_days");
            String statusView = "BLACKLISTED".equals(rs.getString("status")) ? "BLACKLISTED" : "ACTIVE";
            return new AdminUserView(
                    String.valueOf(rs.getLong("id")),
                    rs.getString("name"),
                    rs.getString("user_no"),
                    rs.getString("role"),
                    statusView,
                    rs.getString("department") == null ? "" : rs.getString("department"),
                    rs.getString("phone") == null ? "" : rs.getString("phone"),
                    rs.getString("email") == null ? "" : rs.getString("email"),
                    rs.getInt("borrow_count"),
                    rate.multiply(BigDecimal.valueOf(overdueDays)).setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    iso(rs.getTimestamp("created_at")),
                    iso(rs.getTimestamp("last_login_at")),
                    rs.getString("blacklist_reason"));
        });
    }

    // ------------------------------------------------------------ 审计

    public PageResult<AuditLogView> auditLogs(String category, String keyword, PageQuery page) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (category != null && !category.isBlank() && !"ALL".equals(category)) {
            where.append(" AND a.action LIKE ?");
            params.add(category + "%");
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.name LIKE ? OR a.action LIKE ? OR a.object_type LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String base = """
                FROM audit_log a
                LEFT JOIN sys_user u ON u.id = a.actor_id
                """ + where;
        String select = """
                SELECT a.id, a.action, a.object_type, a.object_id, a.purpose, a.detail, a.ip, a.actor_role,
                       a.created_at, u.name AS actor_name
                """ + base;
        return page(select + " ORDER BY a.created_at DESC", "SELECT COUNT(*) " + base, params, page, (rs, i) -> new AuditLogView(
                String.valueOf(rs.getLong("id")),
                iso(rs.getTimestamp("created_at")),
                new AuditActor(
                        rs.getString("actor_name") == null ? "系统" : rs.getString("actor_name"),
                        rs.getString("actor_role") == null ? "SYSTEM" : rs.getString("actor_role")),
                rs.getString("action"),
                auditCategory(rs.getString("action")),
                rs.getString("object_type") + " #" + rs.getObject("object_id"),
                detailText(rs.getObject("detail")),
                rs.getString("ip") == null ? "" : rs.getString("ip"),
                "",
                null,
                null));
    }

    public AuditLogView auditLog(Long id) {
        return jdbc.query(
                        """
                        SELECT a.id, a.action, a.object_type, a.object_id, a.purpose, a.detail, a.ip, a.actor_role,
                               a.created_at, u.name AS actor_name
                        FROM audit_log a LEFT JOIN sys_user u ON u.id = a.actor_id
                        WHERE a.id = ?
                        """,
                        (rs, i) -> new AuditLogView(
                                String.valueOf(rs.getLong("id")),
                                iso(rs.getTimestamp("created_at")),
                                new AuditActor(
                                        rs.getString("actor_name") == null ? "系统" : rs.getString("actor_name"),
                                        rs.getString("actor_role") == null ? "SYSTEM" : rs.getString("actor_role")),
                                rs.getString("action"),
                                auditCategory(rs.getString("action")),
                                rs.getString("object_type") + " #" + rs.getObject("object_id"),
                                detailText(rs.getObject("detail")),
                                rs.getString("ip") == null ? "" : rs.getString("ip"),
                                "",
                                null,
                                null),
                        id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private String auditCategory(String action) {
        String a = action == null ? "" : action;
        if (a.startsWith("GRACE") || a.startsWith("APPEAL") || a.startsWith("RESERVATION")) {
            return "APPROVAL";
        }
        if (a.startsWith("RULE") || a.startsWith("NOTIFY_TEMPLATE")) {
            return "RULE";
        }
        if (a.startsWith("USER") || a.startsWith("AUTH")) {
            return "USER";
        }
        return "SYSTEM";
    }

    private String detailText(Object detail) {
        if (detail == null) {
            return "";
        }
        if (detail instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (Exception e) {
            return String.valueOf(detail);
        }
    }

    // ------------------------------------------------------------ 借还台

    public CirculationTodayView circulationToday() {
        List<CirculationFlowView> flows = new ArrayList<>(jdbc.query(
                """
                SELECT o.id, o.borrowed_at AS at, 'BORROW' AS type,
                       u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                       b.id AS book_id, b.title, b.author, c.barcode
                FROM borrow_order o
                JOIN sys_user u ON u.id = o.reader_id
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                JOIN book b ON b.id = o.book_id
                LEFT JOIN book_copy c ON c.id = o.copy_id
                WHERE DATE(o.borrowed_at) = CURDATE()
                """,
                flowMapper(false)));
        flows.addAll(jdbc.query(
                """
                SELECT o.id, o.returned_at AS at, 'RETURN' AS type,
                       u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                       b.id AS book_id, b.title, b.author, c.barcode
                FROM borrow_order o
                JOIN sys_user u ON u.id = o.reader_id
                LEFT JOIN reader_profile rp ON rp.user_id = u.id
                JOIN book b ON b.id = o.book_id
                LEFT JOIN book_copy c ON c.id = o.copy_id
                WHERE DATE(o.returned_at) = CURDATE()
                """,
                flowMapper(true)));
        flows.sort(Comparator.comparing(CirculationFlowView::at, Comparator.nullsLast(Comparator.reverseOrder())));
        int borrows = count("SELECT COUNT(*) FROM borrow_order WHERE DATE(borrowed_at) = CURDATE()");
        int returns = count("SELECT COUNT(*) FROM borrow_order WHERE DATE(returned_at) = CURDATE()");
        return new CirculationTodayView(flows.stream().limit(30).toList(), borrows, returns);
    }

    private RowMapper<CirculationFlowView> flowMapper(boolean returned) {
        return (rs, i) -> new CirculationFlowView(
                String.valueOf(rs.getLong("id")),
                iso(rs.getTimestamp("at")),
                rs.getString("type"),
                new Person(
                        String.valueOf(rs.getLong("reader_id")),
                        rs.getString("reader_name"),
                        rs.getString("user_no"),
                        rs.getString("department")),
                new BookRef(
                        String.valueOf(rs.getLong("book_id")),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("barcode")),
                returned ? "馆员 · 归还台" : "馆员 · 借出台",
                "COUNTER");
    }

    // ------------------------------------------------------------ 规则附表

    public List<Map<String, Object>> tiers() {
        return jdbc.query(
                """
                SELECT tier_code, tier_name, min_days, max_days, action_code, tone_code, allow_renew, description
                FROM overdue_tier ORDER BY min_days
                """,
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", rs.getString("tier_code"));
                    row.put("name", rs.getString("tier_name"));
                    row.put("minDays", rs.getInt("min_days"));
                    row.put("maxDays", rs.getObject("max_days"));
                    row.put("actionCode", rs.getString("action_code"));
                    row.put("toneCode", rs.getString("tone_code"));
                    row.put("allowRenew", rs.getInt("allow_renew") == 1);
                    row.put("description", rs.getString("description"));
                    return row;
                });
    }

    public AdminViews.RuleSetView rules() {
        Map<String, String> values = ruleService.all();
        List<AdminViews.RuleOption> fineGate = List.of(
                new AdminViews.RuleOption("T1 温和提醒期", "T1"),
                new AdminViews.RuleOption("T2 可申诉保护期", "T2"),
                new AdminViews.RuleOption("T3 罚金起算期", "T3"),
                new AdminViews.RuleOption("T4 罚金门槛", "T4"));
        List<AdminViews.RuleOption> quietStart = List.of(
                new AdminViews.RuleOption("21:00", "21:00"),
                new AdminViews.RuleOption("22:00", "22:00"),
                new AdminViews.RuleOption("23:00", "23:00"));
        List<AdminViews.RuleOption> quietEnd = List.of(
                new AdminViews.RuleOption("06:00", "06:00"),
                new AdminViews.RuleOption("07:00", "07:00"),
                new AdminViews.RuleOption("08:00", "08:00"),
                new AdminViews.RuleOption("09:00", "09:00"));
        return new AdminViews.RuleSetView(
                List.of(
                        group(
                                "borrow",
                                "借阅与续借",
                                "决定借期、续借次数，以及预约排队对续借的阻断。",
                                item(values, "RENEW.LOAN_DAYS", "借阅天数", "默认借期，从借出当天起算。", "number", "天", 1, 90),
                                item(values, "RENEW.PERIOD_DAYS", "续借周期", "每次续借可延长的天数。", "number", "天", 1, 60),
                                item(values, "RENEW.MAX_TIMES", "最大续借次数", "同一笔借阅最多允许续借的次数。", "number", "次", 0, 5),
                                item(values, "RENEW.QUEUE_BLOCK", "排队阻断人数", "该书预约排队超过该人数时禁止续借。", "number", "人", 0, 50)),
                        group(
                                "grace",
                                "宽限与考试周",
                                "宽限天数按 B_s × K_t × K_q 计算，向上取整到 0.5 天步长并以封顶值收敛。",
                                item(values, "BS.NORMAL", "基准宽限·日常", "日常场景下的基准宽限天数。", "number", "天", 0, 30),
                                item(values, "BS.EXAM", "基准宽限·考试周", "校历命中考试周时的基准天数。", "number", "天", 0, 30),
                                item(values, "BS.THESIS", "基准宽限·论文期", "校历命中论文期时的基准天数。", "number", "天", 0, 30),
                                item(values, "BS.GRADUATION", "基准宽限·毕业季", "校历命中毕业季时的基准天数。", "number", "天", 0, 30),
                                item(values, "GRACE.DMAX", "宽限上限", "任何申请都不会超过的宽限天数。", "number", "天", 1, 30),
                                item(values, "GRACE.ROUND_STEP", "宽限取整步长", "计算结果向上取整的步长。", "number", "天", 0, 2),
                                item(values, "GRACE.SNAPSHOT_TTL_HOURS", "宽限快照有效期", "申请时的规则快照缓存时长。", "number", "小时", 1, 168),
                                item(values, "KT.TIER_0", "信用系数·0 档", "信用最好的读者系数。", "number", null, 0, 1),
                                item(values, "KT.TIER_1", "信用系数·1 档", "轻度逾期读者的系数。", "number", null, 0, 1),
                                item(values, "KT.TIER_2", "信用系数·2 档", "多次逾期读者的系数。", "number", null, 0, 1),
                                item(values, "KT.TIER_3", "信用系数·3 档", "严重逾期读者的系数。", "number", null, 0, 1),
                                item(values, "KT.RECOVER_MONTHS", "信用恢复月数", "连续良好履约多久可升一档。", "number", "月", 1, 24),
                                item(values, "KQ.LEVEL_0", "履约系数·0 级", "无逾期读者系数。", "number", null, 0, 1),
                                item(values, "KQ.LEVEL_1", "履约系数·1 级", "近 12 个月 1 次逾期系数。", "number", null, 0, 1),
                                item(values, "KQ.LEVEL_2", "履约系数·2 级", "近 12 个月 2 次及以上逾期系数。", "number", null, 0, 1)),
                        group(
                                "appeal",
                                "申诉与 SLA",
                                "申诉工单的响应时限与超时补偿。",
                                item(values, "APPEAL.SLA_HOURS", "首次响应时限", "超时未响应将标记 SLA 违约。", "number", "小时", 1, 72),
                                item(values, "APPEAL.AUTO_COMP_DAYS", "超时补偿天数", "SLA 违约时自动补足的借期天数。", "number", "天", 0, 30)),
                        group(
                                "notify",
                                "通知与免打扰",
                                "免打扰窗口内的提醒会平移到窗口结束，预约到书通知除外。",
                                item(values, "QUIET.START", "免打扰开始", "每天进入免打扰的时间点。", "select", null, null, null, quietStart),
                                item(values, "QUIET.END", "免打扰结束", "免打扰结束、待发提醒统一投递的时间点。", "select", null, null, null, quietEnd),
                                item(values, "RESERVATION.READY_HOLD_HOURS", "预约到书保留时长", "到书后未取则顺延给下一位读者。", "number", "小时", 1, 168)),
                        group(
                                "fine",
                                "罚金与审计",
                                "罚金仅在罚金门槛档位之后按天累计，审计日志按保留期归档。",
                                item(values, "FINE.PER_DAY", "罚金费率", "每册每天的罚金金额。", "money", "元/天", 0, 10),
                                item(values, "BORROW.FINE_GATE_TIER", "罚金起算档位", "达到该逾期档位后才开始计费。", "select", null, null, null, fineGate),
                                item(values, "AUDIT.RETENTION_DAYS", "审计保留天数", "审计日志的最小保留期限。", "number", "天", 7, 365))),
                ruleService.version());
    }

    private AdminViews.RuleGroupView group(String id, String title, String description, AdminViews.RuleItemView... items) {
        return new AdminViews.RuleGroupView(id, title, description, List.of(items));
    }

    private AdminViews.RuleItemView item(
            Map<String, String> values,
            String key,
            String label,
            String description,
            String type,
            String unit,
            Integer min,
            Integer max) {
        return item(values, key, label, description, type, unit, min, max, null);
    }

    private AdminViews.RuleItemView item(
            Map<String, String> values,
            String key,
            String label,
            String description,
            String type,
            String unit,
            Integer min,
            Integer max,
            List<AdminViews.RuleOption> options) {
        return new AdminViews.RuleItemView(
                key, label, description, type, values.getOrDefault(key, ""), unit, options, min, max);
    }

    public List<Map<String, Object>> calendar() {        return jdbc.query(
                """
                SELECT id, term, scene_code, start_date, end_date, source, note
                FROM academic_calendar WHERE deleted = 0 ORDER BY start_date DESC
                """,
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", String.valueOf(rs.getLong("id")));
                    row.put("term", rs.getString("term"));
                    row.put("scene", rs.getString("scene_code"));
                    row.put("startDate", rs.getDate("start_date").toString());
                    row.put("endDate", rs.getDate("end_date").toString());
                    row.put("source", rs.getString("source"));
                    row.put("note", rs.getString("note"));
                    return row;
                });
    }

    // ------------------------------------------------------------ 工具

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private BigDecimal sumOverdueFine() {
        BigDecimal rate = ruleService.decimal("FINE.PER_DAY", "0.50");
        Integer days = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), effective_due_at), 0)), 0)
                FROM borrow_order WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at < CURDATE()
                """,
                Integer.class);
        return rate.multiply(BigDecimal.valueOf(days == null ? 0 : days));
    }

    private <T> PageResult<T> page(
            String selectSql, String countSql, List<Object> params, PageQuery page, RowMapper<T> mapper) {
        Long total = jdbc.queryForObject(countSql, Long.class, params.toArray());
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(page.size());
        pageParams.add(page.offset());
        List<T> items = jdbc.query(selectSql + " LIMIT ? OFFSET ?", mapper, pageParams.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page.page(), page.size());
    }

    private static OffsetDateTime at(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().atZone(ZONE).toOffsetDateTime();
    }

    private static String isoPlusHours(java.sql.Timestamp timestamp, int hours) {
        OffsetDateTime at = at(timestamp);
        return at == null ? null : at.plusHours(hours).toString();
    }

    private static String iso(java.sql.Timestamp timestamp) {
        OffsetDateTime at = at(timestamp);
        return at == null ? null : at.toString();
    }

    private static String levelName(String level) {
        return switch (level) {
            case "L1" -> "到期前提醒";
            case "L2" -> "到期当天提醒";
            case "L3" -> "逾期提醒";
            case "GRACE_RESULT" -> "宽限结果通知";
            case "APPEAL_SLA" -> "申诉受理通知";
            case "RESERVATION_READY" -> "预约到书通知";
            default -> "系统通知";
        };
    }

    private static String tone(String level) {
        return switch (level) {
            case "L1", "RESERVATION_READY" -> "info";
            case "L2" -> "warning";
            case "L3", "APPEAL_SLA" -> "danger";
            default -> "neutral";
        };
    }

}

