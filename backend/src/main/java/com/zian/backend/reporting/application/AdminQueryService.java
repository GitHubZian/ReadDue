package com.zian.backend.reporting.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.notify.application.NotifyTemplateService;
import com.zian.backend.notify.domain.repository.NotifyTemplateRepository;
import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.reporting.domain.repository.AdminReadRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理端只读查询服务：跨上下文聚合统一走 {@link com.zian.backend.reporting.domain.repository.AdminReadRepository}，不触碰任何业务上下文的 Mapper。
 */
@Service
@RequiredArgsConstructor
public class AdminQueryService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM-dd");

    private final AdminReadRepository readRepository;
    private final RuleService ruleService;
    private final NotifyTemplateRepository templateRepository;
    private final NotifyTemplateService templateService;
    private final ObjectMapper objectMapper;

// region 工作台

    /**
     * 工作台聚合：待办、趋势、规则命中与通知。
     *
     * @return 工作台视图
     */
    public DashboardData dashboard() {
        int pendingGraces = readRepository.countPendingGraces();
        int pendingAppeals = readRepository.countPendingAppeals();
        int dueToday = readRepository.countDueToday();
        int overdueBooks = readRepository.countOverdueBooks();
        int overdueReaders = readRepository.countOverdueReaders();
        int slaBreach = readRepository.countSlaBreach();
        BigDecimal rate = ruleService.decimal("FINE.PER_DAY", "0.50");
        String fine = rate.multiply(BigDecimal.valueOf(readRepository.sumOverdueDays()))
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
        DashboardStats stats = new DashboardStats(
                pendingGraces,
                pendingAppeals,
                dueToday,
                overdueBooks,
                overdueReaders,
                fine,
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
        readRepository.borrowTrend(from.atStartOfDay())
                .forEach(row -> bucket(buckets, row, 0));
        readRepository.returnTrend(from.atStartOfDay())
                .forEach(row -> bucket(buckets, row, 1));
        List<TrendPoint> points = new ArrayList<>();
        buckets.forEach((day, counts) -> points.add(
                new TrendPoint(day.toString(), day.format(DAY), counts[0], counts[1])));
        return points;
    }

    private void bucket(Map<LocalDate, int[]> buckets, Map<String, Object> row, int index) {
        LocalDate day = date(row.get("d"));
        int[] counts = day == null ? null : buckets.get(day);
        if (counts != null) {
            counts[index] = integer(row.get("c"), 0);
        }
    }

    private List<NoticeItem> notices() {
        return readRepository.notices().stream()
                .map(row -> new NoticeItem(
                        String.valueOf(row.get("id")),
                        tone(str(row, "level")),
                        levelName(str(row, "level")),
                        (str(row, "reader_name") == null ? "读者" : str(row, "reader_name")) + " · 已送达站内信",
                        iso(row.get("plan_at"))))
                .toList();
    }

    private List<DueTodayItem> dueTodayItems() {
        return readRepository.dueTodayItems().stream()
                .map(row -> new DueTodayItem(
                        String.valueOf(row.get("id")),
                        new Person(
                                str(row, "reader_id"),
                                str(row, "reader_name"),
                                str(row, "user_no"),
                                str(row, "department")),
                        new BookRef(
                                String.valueOf(row.get("book_id")),
                                str(row, "title"),
                                str(row, "author"),
                                str(row, "barcode")),
                        dateText(row.get("effective_due_at")),
                        false))
                .toList();
    }

// endregion

// region 宽限

    /**
     * 宽限单分页查询，含审批历史与 SLA。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param page    分页参数
     * @return 宽限单分页
     */
    public PageResult<GraceRecordView> graces(String status, String keyword, PageQuery page) {
        long total = readRepository.countGraces(status, keyword);
        List<GraceRecordView> items = readRepository
                .graces(status, keyword, page.size(), page.offset())
                .stream()
                .map(this::graceView)
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

    private GraceRecordView graceView(Map<String, Object> row) {
        String dbStatus = str(row, "status");
        Integer approved = row.get("granted_days") == null ? null : integer(row.get("granted_days"), 0);
        List<TimelineItem> history = new ArrayList<>();
        history.add(new TimelineItem(
                iso(row.get("created_at")),
                str(row, "reader_name"),
                "提交宽限申请 · 场景 " + str(row, "scene_code")));
        if (row.get("decided_at") != null) {
            history.add(new TimelineItem(
                    iso(row.get("decided_at")),
                    row.get("decided_by") == null ? "馆员" : String.valueOf(row.get("decided_by")),
                    ("APPROVED".equals(dbStatus) ? "通过" : "驳回")
                            + (approved == null ? "" : "，宽限 " + approved + " 天")));
        }
        int requested = (int) Math.ceil(decimal(row.get("computed_days"), BigDecimal.ZERO).doubleValue());
        return new GraceRecordView(
                String.valueOf(row.get("id")),
                str(row, "apply_no"),
                new Person(
                        String.valueOf(row.get("reader_id")),
                        str(row, "reader_name"),
                        str(row, "user_no"),
                        str(row, "department")),
                new BookRef(
                        String.valueOf(row.get("book_id")),
                        str(row, "title"),
                        str(row, "author"),
                        str(row, "barcode")),
                str(row, "scene_code"),
                str(row, "evidence_text"),
                graceStatus(dbStatus),
                requested,
                approved,
                isoPlusHours(row.get("created_at"), ruleService.intValue("APPEAL.SLA_HOURS", 2)),
                iso(row.get("created_at")),
                List.of(),
                history);
    }

    private String graceStatus(String db) {
        return switch (db) {
            case "APPROVED" -> "APPROVED";
            case "REJECTED" -> "REJECTED";
            case "EXPIRED" -> "EXPIRED";
            default -> "PENDING";
        };
    }

// endregion

// region 申诉

    /**
     * 申诉工单分页查询，含时间线与结论。
     *
     * @param status   状态过滤
     * @param category 分类过滤
     * @param keyword  关键字
     * @param page     分页参数
     * @return 工单分页
     */
    public PageResult<AppealTicketView> appeals(String status, String category, String keyword, PageQuery page) {
        long total = readRepository.countAppeals(status, category, keyword);
        List<AppealTicketView> items = readRepository
                .appeals(status, category, keyword, page.size(), page.offset())
                .stream()
                .map(this::appealView)
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

    private AppealTicketView appealView(Map<String, Object> row) {
        String dbStatus = str(row, "status");
        List<TimelineItem> timeline = new ArrayList<>();
        timeline.add(new TimelineItem(
                iso(row.get("created_at")), str(row, "reader_name"), "提交申诉：" + str(row, "reason")));
        if (row.get("first_reply_at") != null) {
            timeline.add(new TimelineItem(
                    iso(row.get("first_reply_at")),
                    str(row, "assignee_name") == null ? "馆员" : str(row, "assignee_name"),
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
                    row.get("compensation_days") == null ? null : integer(row.get("compensation_days"), 0),
                    str(row, "resolution") == null ? "" : str(row, "resolution"),
                    iso(row.get("updated_at")),
                    str(row, "assignee_name") == null ? "系统" : str(row, "assignee_name"));
            timeline.add(new TimelineItem(
                    iso(row.get("updated_at")),
                    str(row, "assignee_name") == null ? "系统" : str(row, "assignee_name"),
                    "结论：" + conclusion.note()));
        }
        Object bookId = row.get("book_id");
        return new AppealTicketView(
                String.valueOf(row.get("id")),
                str(row, "ticket_no"),
                new Person(
                        String.valueOf(row.get("reader_id")),
                        str(row, "reader_name"),
                        str(row, "user_no"),
                        str(row, "department")),
                new BookRef(
                        bookId == null ? null : String.valueOf(bookId),
                        str(row, "title") == null ? "关联借阅单" : str(row, "title"),
                        str(row, "author") == null ? "" : str(row, "author"),
                        str(row, "barcode")),
                appealCategory(str(row, "target_type")),
                appealStatus(dbStatus),
                str(row, "reason"),
                iso(row.get("created_at")),
                iso(row.get("sla_due_at")),
                conclusion,
                timeline);
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

// endregion

// region 预约

    /**
     * 预约分页查询。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param page    分页参数
     * @return 预约分页
     */
    public PageResult<ReservationView> reservations(String status, String keyword, PageQuery page) {
        long total = readRepository.countReservations(status, keyword);
        List<ReservationView> items = readRepository
                .reservations(status, keyword, page.size(), page.offset())
                .stream()
                .map(row -> new ReservationView(
                        String.valueOf(row.get("id")),
                        new Person(
                                String.valueOf(row.get("reader_id")),
                                str(row, "reader_name"),
                                str(row, "user_no"),
                                str(row, "department")),
                        new BookRef(
                                String.valueOf(row.get("book_id")),
                                str(row, "title"),
                                str(row, "author"),
                                null),
                        iso(row.get("created_at")),
                        integer(row.get("queue_no"), 0),
                        str(row, "status"),
                        iso(row.get("ready_at")),
                        iso(row.get("expires_at")),
                        "READY".equals(str(row, "status")) ? "预约架 A-02" : null))
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

// endregion

// region 逾期

    /**
     * 逾期看板分页查询，罚金按费率实时计算。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param page    分页参数
     * @return 逾期条目分页
     */
    public PageResult<OverdueItemView> overdue(String status, String keyword, PageQuery page) {
        long total = readRepository.countOverdue(status, keyword);
        BigDecimal rate = ruleService.decimal("FINE.PER_DAY", "0.50");
        List<OverdueItemView> items = readRepository
                .overdue(status, keyword, page.size(), page.offset())
                .stream()
                .map(row -> {
                    int days = Math.max(integer(row.get("overdue_days"), 0), 0);
                    return new OverdueItemView(
                            String.valueOf(row.get("order_id")),
                            new Person(
                                    String.valueOf(row.get("reader_id")),
                                    str(row, "reader_name"),
                                    str(row, "student_no"),
                                    str(row, "department")),
                            new BookRef(
                                    String.valueOf(row.get("book_id")),
                                    str(row, "book_title"),
                                    str(row, "author"),
                                    str(row, "barcode")),
                            dateText(row.get("effective_due_at")),
                            days,
                            rate.multiply(BigDecimal.valueOf(days))
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toPlainString(),
                            integer(row.get("l3_sent"), 0),
                            iso(row.get("last_reminded")),
                            "LOST".equals(str(row, "status")) ? "LOST" : "OVERDUE");
                })
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

// endregion

// region 提醒

    /**
     * 通知模板全量列表。
     *
     * @return 模板视图列表
     */
    public List<NotifyDtos.TemplateView> templates() {
        return templateRepository.findAll().stream().map(templateService::view).toList();
    }

    /**
     * 通知任务分页查询，状态按前端枚举映射。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param page    分页参数
     * @return 任务分页
     */
    public PageResult<NotifyTaskView> notifyTasks(String status, String keyword, PageQuery page) {
        String mapped = switch (status == null ? "" : status) {
            case "PENDING" -> "PLANNED";
            case "SENDING" -> "QUEUED";
            default -> status;
        };
        long total = readRepository.countNotifyTasks(mapped, keyword);
        List<NotifyTaskView> items = readRepository
                .notifyTasks(mapped, keyword, page.size(), page.offset())
                .stream()
                .map(row -> {
                    String dbStatus = str(row, "status");
                    String statusView = switch (dbStatus) {
                        case "PLANNED", "CANCELED", "MERGED" -> "PENDING";
                        case "QUEUED" -> "SENDING";
                        default -> dbStatus;
                    };
                    boolean sent = "SENT".equals(dbStatus);
                    boolean failed = "FAILED".equals(dbStatus);
                    String levelName = levelName(str(row, "level"));
                    return new NotifyTaskView(
                            String.valueOf(row.get("id")),
                            levelName + " · " + (str(row, "reader_name") == null ? "读者" : str(row, "reader_name")),
                            levelName,
                            "INAPP",
                            1,
                            sent ? 1 : 0,
                            failed ? 1 : 0,
                            statusView,
                            iso(row.get("created_at")),
                            sent ? iso(row.get("updated_at")) : null,
                            failed ? "投递失败，可重试" : null);
                })
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

// endregion

// region 用户

    /**
     * 用户分页查询，含在借数量与累计罚金。
     *
     * @param role    角色过滤
     * @param status  状态过滤
     * @param keyword 关键字
     * @param page    分页参数
     * @return 用户分页
     */
    public PageResult<AdminUserView> users(String role, String status, String keyword, PageQuery page) {
        String mappedStatus = status == null || status.isBlank() || "ALL".equals(status)
                ? status
                : ("ACTIVE".equals(status) ? "NORMAL" : status);
        long total = readRepository.countUsers(role, mappedStatus, keyword);
        BigDecimal rate = ruleService.decimal("FINE.PER_DAY", "0.50");
        List<AdminUserView> items = readRepository
                .users(role, mappedStatus, keyword, page.size(), page.offset())
                .stream()
                .map(row -> {
                    int overdueDays = integer(row.get("overdue_days"), 0);
                    String statusView = "BLACKLISTED".equals(str(row, "status")) ? "BLACKLISTED" : "ACTIVE";
                    return new AdminUserView(
                            String.valueOf(row.get("id")),
                            str(row, "name"),
                            str(row, "user_no"),
                            str(row, "role"),
                            statusView,
                            valueOr(row, "department"),
                            valueOr(row, "phone"),
                            valueOr(row, "email"),
                            integer(row.get("borrow_count"), 0),
                            rate.multiply(BigDecimal.valueOf(overdueDays))
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toPlainString(),
                            iso(row.get("created_at")),
                            iso(row.get("last_login_at")),
                            str(row, "blacklist_reason"));
                })
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

// endregion

// region 审计

    /**
     * 审计日志分页查询。
     *
     * @param category 分类过滤
     * @param keyword  关键字
     * @param page     分页参数
     * @return 日志分页
     */
    public PageResult<AuditLogView> auditLogs(String category, String keyword, PageQuery page) {
        long total = readRepository.countAuditLogs(category, keyword);
        List<AuditLogView> items = readRepository
                .auditLogs(category, keyword, page.size(), page.offset())
                .stream()
                .map(this::auditView)
                .toList();
        return new PageResult<>(items, total, page.page(), page.size());
    }

    /**
     * 审计日志详情。
     *
     * @param id 日志 ID
     * @return 日志视图，不存在时为 null
     */
    public AuditLogView auditLog(Long id) {
        Map<String, Object> row = readRepository.auditLog(id);
        return row == null ? null : auditView(row);
    }

    private AuditLogView auditView(Map<String, Object> row) {
        return new AuditLogView(
                String.valueOf(row.get("id")),
                iso(row.get("created_at")),
                new AuditActor(
                        str(row, "actor_name") == null ? "系统" : str(row, "actor_name"),
                        str(row, "actor_role") == null ? "SYSTEM" : str(row, "actor_role")),
                str(row, "action"),
                auditCategory(str(row, "action")),
                str(row, "object_type") + " #" + row.get("object_id"),
                detailText(row.get("detail")),
                str(row, "ip") == null ? "" : str(row, "ip"),
                "",
                null,
                null);
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

// endregion

// region 借还台

    /**
     * 今日借还台流水与计数。
     *
     * @return 流通视图
     */
    public CirculationTodayView circulationToday() {
        List<CirculationFlowView> flows = new ArrayList<>();
        readRepository.circulationBorrows().forEach(row -> flows.add(flow(row, "馆员 · 借出台")));
        readRepository.circulationReturns().forEach(row -> flows.add(flow(row, "馆员 · 归还台")));
        flows.sort(Comparator.comparing(CirculationFlowView::at, Comparator.nullsLast(Comparator.reverseOrder())));
        return new CirculationTodayView(
                flows.stream().limit(30).toList(),
                readRepository.countBorrowsToday(),
                readRepository.countReturnsToday());
    }

    private CirculationFlowView flow(Map<String, Object> row, String operator) {
        return new CirculationFlowView(
                String.valueOf(row.get("id")),
                iso(row.get("at")),
                str(row, "type"),
                new Person(
                        String.valueOf(row.get("reader_id")),
                        str(row, "reader_name"),
                        str(row, "user_no"),
                        str(row, "department")),
                new BookRef(
                        String.valueOf(row.get("book_id")),
                        str(row, "title"),
                        str(row, "author"),
                        str(row, "barcode")),
                operator,
                "COUNTER");
    }

// endregion

// region 规则附表

    /**
     * 逾期分档表。
     *
     * @return 分档列表（Map 形式，字段与前端对齐）
     */
    public List<Map<String, Object>> tiers() {
        return readRepository.tiers().stream()
                .map(row -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("code", str(row, "code"));
                    view.put("name", str(row, "name"));
                    view.put("minDays", integer(row.get("minDays"), 0));
                    view.put("maxDays", row.get("maxDays") == null ? null : integer(row.get("maxDays"), 0));
                    view.put("actionCode", str(row, "actionCode"));
                    view.put("toneCode", str(row, "toneCode"));
                    view.put("allowRenew", truthy(row.get("allowRenew")));
                    view.put("description", str(row, "description"));
                    return view;
                })
                .toList();
    }

    /**
     * 规则参数集，按业务分组并转为前端可编辑结构。
     *
     * @return 规则分组与版本
     */
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

    /**
     * 校历列表，可能为空。
     *
     * @return 校历条目
     */
    public List<Map<String, Object>> calendar() {
        return readRepository.calendar().stream()
                .map(row -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("id", String.valueOf(row.get("id")));
                    view.put("term", str(row, "term"));
                    view.put("scene", str(row, "scene"));
                    view.put("startDate", dateText(row.get("startDate")));
                    view.put("endDate", dateText(row.get("endDate")));
                    view.put("source", str(row, "source"));
                    view.put("note", str(row, "note"));
                    return view;
                })
                .toList();
    }

// endregion

// region 值转换


    private static String str(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static String valueOr(Map<String, Object> row, String key) {
        String value = str(row, key);
        return value == null ? "" : value;
    }

    private static int integer(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private static boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static BigDecimal decimal(Object value, BigDecimal fallback) {
        if (value == null) {
            return fallback;
        }
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }

    private static LocalDate date(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.length() >= 10 ? LocalDate.parse(text.substring(0, 10)) : null;
    }

    private static String dateText(Object value) {
        LocalDate date = date(value);
        return date == null ? "" : date.toString();
    }

    private static LocalDateTime dateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.atZoneSameInstant(ZONE).toLocalDateTime();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        return value == null ? null : LocalDateTime.parse(String.valueOf(value));
    }

    private static String iso(Object value) {
        LocalDateTime time = dateTime(value);
        return time == null ? null : time.atZone(ZONE).toOffsetDateTime().toString();
    }

    private static String isoPlusHours(Object value, int hours) {
        LocalDateTime time = dateTime(value);
        return time == null ? null : time.plusHours(hours).atZone(ZONE).toOffsetDateTime().toString();
    }

    private static String levelName(String level) {
        return switch (level == null ? "" : level) {
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
        return switch (level == null ? "" : level) {
            case "L1", "RESERVATION_READY" -> "info";
            case "L2" -> "warning";
            case "L3", "APPEAL_SLA" -> "danger";
            default -> "neutral";
        };
    }

// endregion

}
