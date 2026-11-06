package com.zian.backend.reporting.interfaces;

import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.reporting.application.AdminQueryService;
import com.zian.backend.reporting.interfaces.dto.AdminViews;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ErrorCode;
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

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('workbench:read')")
    public BaseResponse<AdminViews.DashboardData> dashboard() {
        return ResultUtils.success(queryService.dashboard());
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<PageResult<AdminViews.AdminUserView>> users(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.users(role, status, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/graces")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<PageResult<AdminViews.GraceRecordView>> graces(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.graces(status, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/appeals")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<PageResult<AdminViews.AppealTicketView>> appeals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.appeals(status, category, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/reservations")
    @PreAuthorize("hasAuthority('reservation:manage')")
    public BaseResponse<PageResult<AdminViews.ReservationView>> reservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.reservations(status, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('overdue:read')")
    public BaseResponse<PageResult<AdminViews.OverdueItemView>> overdue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.overdue(status, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/notify-tasks")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<PageResult<AdminViews.NotifyTaskView>> notifyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.notifyTasks(status, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<List<NotifyDtos.TemplateView>> templates() {
        return ResultUtils.success(queryService.templates());
    }

    @GetMapping("/rules")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<AdminViews.RuleSetView> rules() {
        return ResultUtils.success(queryService.rules());
    }

    @GetMapping("/tiers")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<List<Map<String, Object>>> tiers() {
        return ResultUtils.success(queryService.tiers());
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<List<Map<String, Object>>> calendar() {
        return ResultUtils.success(queryService.calendar());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('audit:read')")
    public BaseResponse<PageResult<AdminViews.AuditLogView>> auditLogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(queryService.auditLogs(category, keyword, PageQuery.of(page, size, sort)));
    }

    @GetMapping("/audit-logs/{id}")
    @PreAuthorize("hasAuthority('audit:read')")
    public BaseResponse<AdminViews.AuditLogView> auditLog(@PathVariable Long id) {
        AdminViews.AuditLogView view = queryService.auditLog(id);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "审计日志不存在");
        }
        return ResultUtils.success(view);
    }

    @GetMapping("/audit-logs/export")
    @PreAuthorize("hasAuthority('audit:read')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) String category, @RequestParam(required = false) String keyword) {
        PageResult<AdminViews.AuditLogView> result =
                queryService.auditLogs(category, keyword, PageQuery.of(1, PageQuery.MAX_SIZE, null));
        StringBuilder csv = new StringBuilder("id,时间,操作人,角色,动作,分类,对象,IP,详情\n");
        result.items().forEach(log -> {
            csv.append(String.join(",",
                            csvCell(log.id()),
                            csvCell(log.at() == null ? "" : log.at().toString()),
                            csvCell(log.actor().name()),
                            csvCell(log.actor().role()),
                            csvCell(log.action()),
                            csvCell(log.category()),
                            csvCell(log.target()),
                            csvCell(log.ip()),
                            csvCell(log.detail())))
                    .append('\n');
        });
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

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

