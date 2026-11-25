package com.zian.backend.reporting.interfaces;

import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.reporting.application.AdminQueryService;
import com.zian.backend.reporting.interfaces.dto.AdminViews;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.Masks;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.ResultUtils;
import com.zian.backend.shared.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端读接口：全部为只读 SQL 投影，避免跨上下文直连 Mapper。
 */
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminQueryController {

    private final AdminQueryService queryService;

    /**
     * 工作台汇总：待办数量、7 日借还趋势、规则命中、通知与今日到期清单。
     *
     * @return 工作台视图
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('workbench:read')")
    public BaseResponse<AdminViews.DashboardData> dashboard() {
        return ResultUtils.success(queryService.dashboard());
    }

    /**
     * 用户列表：包含学院、在借数量、累计罚金与最近活跃时间。
     *
     * @param role    角色过滤（READER/LIBRARIAN/ADMIN）
     * @param status  状态过滤（ACTIVE/BLACKLISTED/MUTED）
     * @param keyword 姓名或学号关键字
     * @param page    页码，从 1 开始
     * @param size    每页条数，上限 200
     * @return 分页后的用户视图
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<PageResult<AdminViews.AdminUserView>> users(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.users(role, status, keyword, PageQuery.of(page, size)));
    }

    /**
     * 用户敏感详情（查看时后端自动记录 DATA_READ 审计）。
     *
     * @param id 用户主键
     * @return 用户视图
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<AdminViews.AdminUserView> user(@PathVariable long id) {
        return ResultUtils.success(queryService.user(id));
    }

    /**
     * 宽限申请列表，含申请场景、计算天数、审批历史与 SLA 截止时间。
     *
     * @param status  审批状态（PENDING/APPROVED/REJECTED/EXPIRED）
     * @param keyword 姓名、学号或申请单号关键字
     * @param page    页码
     * @param size    每页条数
     * @return 分页后的宽限单
     */
    @GetMapping("/graces")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<PageResult<AdminViews.GraceRecordView>> graces(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.graces(status, keyword, PageQuery.of(page, size)));
    }

    /**
     * 宽限单详情，含证据附件与审批历史，供管理端审批弹层使用。
     *
     * @param id 宽限申请 id
     * @return 宽限单视图
     */
    @GetMapping("/graces/{id}")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<AdminViews.GraceRecordView> grace(@PathVariable long id) {
        return ResultUtils.success(queryService.grace(id));
    }
    /**
     * 申诉工单列表，含分类、SLA、时间线与结论。
     *
     * @param status   处理状态（PENDING/PROCESSING/RESOLVED/REJECTED）
     * @param category 分类（OVERDUE_DISPUTE/FINE_DISPUTE/BLACKLIST/OTHER）
     * @param keyword  姓名、学号或工单号关键字
     * @param page     页码
     * @param size     每页条数
     * @return 分页后的工单
     */
    @GetMapping("/appeals")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<PageResult<AdminViews.AppealTicketView>> appeals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.appeals(status, category, keyword, PageQuery.of(page, size)));
    }

    /**
     * 预约列表，含排队序号、到书时间与取书截止时间。
     *
     * @param status  预约状态（WAITING/READY/FULFILLED/EXPIRED/CANCELLED）
     * @param keyword 姓名、学号或书名关键字
     * @param page    页码
     * @param size    每页条数
     * @return 分页后的预约
     */
    @GetMapping("/reservations")
    @PreAuthorize("hasAuthority('reservation:manage')")
    public BaseResponse<PageResult<AdminViews.ReservationView>> reservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.reservations(status, keyword, PageQuery.of(page, size)));
    }

    /**
     * 逾期看板：逾期天数、罚金、催还次数与最近提醒时间。
     *
     * @param status  状态过滤（OVERDUE/LOST）
     * @param keyword 姓名、学号或书名关键字
     * @param page    页码
     * @param size    每页条数
     * @return 分页后的逾期条目
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('overdue:read')")
    public BaseResponse<PageResult<AdminViews.OverdueItemView>> overdue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.overdue(status, keyword, PageQuery.of(page, size)));
    }

    /**
     * 通知任务列表：展示渠道、目标数、成功/失败数与状态。
     *
     * @param status  状态过滤（PENDING/SENDING/SENT/PARTIAL/FAILED）
     * @param keyword 读者姓名或去重键关键字
     * @param page    页码
     * @param size    每页条数
     * @return 分页后的通知任务
     */
    @GetMapping("/notify-tasks")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<PageResult<AdminViews.NotifyTaskView>> notifyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.notifyTasks(status, keyword, PageQuery.of(page, size)));
    }

    /**
     * 通知模板全量列表（非分页），供管理端编辑。
     *
     * @return 模板视图列表
     */
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<List<NotifyDtos.TemplateView>> templates() {
        return ResultUtils.success(queryService.templates());
    }

    /**
     * 规则参数集：按业务分组返回当前值与版本号，供管理端整组保存。
     *
     * @return 规则分组与版本
     */
    @GetMapping("/rules")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<AdminViews.RuleSetView> rules() {
        return ResultUtils.success(queryService.rules());
    }

    /**
     * 逾期分档表（T1–T4），用于解释逾期处置与罚金门槛。
     *
     * @return 分档列表
     */
    @GetMapping("/tiers")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<List<Map<String, Object>>> tiers() {
        return ResultUtils.success(queryService.tiers());
    }

    /**
     * 校历区间：按学期与场景返回可命中宽限加成的日期范围，可能为空。
     *
     * @return 校历条目列表
     */
    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<List<Map<String, Object>>> calendar() {
        return ResultUtils.success(queryService.calendar());
    }

    /**
     * 审计日志分页查询。
     *
     * @param category 分类过滤（APPROVAL/RULE/USER/SYSTEM/EXCEPTION）
     * @param keyword  操作人、动作或对象关键字
     * @param page     页码
     * @param size     每页条数
     * @return 分页后的审计日志
     */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('audit:read')")
    public BaseResponse<PageResult<AdminViews.AuditLogView>> auditLogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(queryService.auditLogs(category, keyword, PageQuery.of(page, size)));
    }

    /**
     * 审计日志详情，包含变更前后快照。
     *
     * @param id 日志 ID
     * @return 审计日志视图；不存在时抛 404
     */
    @GetMapping("/audit-logs/{id}")
    @PreAuthorize("hasAuthority('audit:read')")
    public BaseResponse<AdminViews.AuditLogView> auditLog(@PathVariable Long id) {
        AdminViews.AuditLogView view = queryService.auditLog(id);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "审计日志不存在");
        }
        return ResultUtils.success(view);
    }

    /**
     * 敏感数据读取记录：谁在何时查看了哪个用户/宽限单。
     *
     * @param targetType 目标类型（USER/GRACE），空表示不过滤
     * @param targetId   目标 ID，null 表示不过滤
     * @param limit      返回条数，默认 20，最大 200
     * @return 读取记录，时间倒序
     */
    @GetMapping("/data-reads")
    @PreAuthorize("hasAuthority('audit:read')")
    public BaseResponse<List<AdminViews.DataReadView>> dataReads(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        return ResultUtils.success(queryService.dataReads(targetType, targetId, limit));
    }

    /**
     * 导出审计日志为 CSV（UTF-8，最多 200 条）。
     *
     * @param category 分类过滤
     * @param keyword  关键字过滤
     * @return CSV 字节流，带 attachment 文件名
     */
    @GetMapping("/audit-logs/export")
    @PreAuthorize("hasAuthority('audit:read')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) String category, @RequestParam(required = false) String keyword) {
        PageResult<AdminViews.AuditLogView> result =
                queryService.auditLogs(category, keyword, PageQuery.of(1, PageQuery.MAX_SIZE));
        StringBuilder csv = new StringBuilder("id,时间,操作人,角色,动作,分类,对象,IP,详情,traceId,变更前,变更后\n");
        // 导出是离线文件，最易泄露个人信息：姓名/IP/详情与变更前后统一脱敏后再写 CSV。
        result.items().forEach(log -> {
            csv.append(String.join(",",
                            csvCell(log.id()),
                            csvCell(log.at() == null ? "" : log.at().toString()),
                            csvCell(Masks.name(log.actor().name())),
                            csvCell(log.actor().role()),
                            csvCell(log.action()),
                            csvCell(log.category()),
                            csvCell(log.target()),
                            csvCell(Masks.ip(log.ip())),
                            csvCell(Masks.json(log.detail(), 500)),
                            csvCell(log.traceId()),
                            csvCell(Masks.json(log.before(), 500)),
                            csvCell(Masks.json(log.after(), 500))))
                    .append('\n');
        });
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    /**
     * 今日借还台流水与借出/归还计数。
     *
     * @return 今日流通视图
     */
    @GetMapping("/circulation/today")
    @PreAuthorize("hasAuthority('borrow:read:all')")
    public BaseResponse<AdminViews.CirculationTodayView> circulationToday() {
        return ResultUtils.success(queryService.circulationToday());
    }

    private String csvCell(String raw) {
        String value = raw == null ? "" : raw;
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

