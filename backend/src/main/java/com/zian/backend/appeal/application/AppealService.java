package com.zian.backend.appeal.application;

import com.zian.backend.appeal.domain.model.AppealStatus;
import com.zian.backend.appeal.domain.model.AppealTicket;
import com.zian.backend.appeal.domain.repository.AppealTicketRepository;
import com.zian.backend.appeal.interfaces.dto.AppealDtos;
import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.application.BorrowService;
import com.zian.backend.grace.application.GraceService;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 申诉工单应用服务：受理 / 通过 / 驳回 / 补偿，以及 SLA 超时自动补偿。
 */
@Service
@RequiredArgsConstructor
public class AppealService {

    private static final Logger log = LoggerFactory.getLogger(AppealService.class);
    private static final Set<String> TARGET_TYPES =
            Set.of("GRACE", "OVERDUE_TIER", "RENEW", "REMINDER", "OTHER");
    private static final DateTimeFormatter TICKET_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AppealTicketRepository repository;
    private final RuleService ruleService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;
    private final GraceService graceService;
    private final BorrowService borrowService;

    /**
     * 受理工单：记录首次回应时间并进入处理中。
     *
     * @param id 工单 ID
     * @return 工单动作视图
     */
    @Transactional
    public AppealDtos.AppealActionView ack(Long id) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        ticket.ack(operator.id(), operator.name(), LocalDateTime.now());
        persist(ticket);
        auditRecorder.record("APPEAL_ACK", "APPEAL_TICKET", id, "申诉受理", Map.of());
        return AppealDtos.AppealActionView.of(ticket);
    }

    /**
     * 给出处理结论并记录说明。
     *
     * @param id      工单 ID
     * @param request 说明与补偿天数
     * @return 工单动作视图
     */
    @Transactional
    public AppealDtos.AppealActionView resolve(Long id, AppealDtos.ActionRequest request) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        Map<String, Object> before = Map.of("status", ticket.getStatus().name());
        ticket.resolve(note(request), operator.id(), operator.name());
        persist(ticket);
        auditRecorder.record(
                "APPEAL_RESOLVE",
                "APPEAL_TICKET",
                id,
                "申诉处理",
                Map.of("note", note(request)),
                before,
                Map.of("status", ticket.getStatus().name()));
        return AppealDtos.AppealActionView.of(ticket);
    }

    /**
     * 驳回工单。
     *
     * @param id      工单 ID
     * @param request 驳回说明
     * @return 工单动作视图
     */
    @Transactional
    public AppealDtos.AppealActionView reject(Long id, AppealDtos.ActionRequest request) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        Map<String, Object> before = Map.of("status", ticket.getStatus().name());
        ticket.reject(note(request), operator.id(), operator.name());
        persist(ticket);
        auditRecorder.record(
                "APPEAL_REJECT",
                "APPEAL_TICKET",
                id,
                "申诉处理",
                Map.of("note", note(request)),
                before,
                Map.of("status", ticket.getStatus().name()));
        return AppealDtos.AppealActionView.of(ticket);
    }

    /**
     * 执行补偿：补还逾期天数。
     *
     * @param id      工单 ID
     * @param request 补偿天数与说明
     * @return 工单动作视图
     */
    @Transactional
    public AppealDtos.AppealActionView compensate(Long id, AppealDtos.ActionRequest request) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        Map<String, Object> before = Map.of("status", ticket.getStatus().name());
        int days = request != null && request.days() != null ? request.days() : 3;
        ticket.compensate(days, note(request), operator.id(), operator.name());
        persist(ticket);
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", ticket.getStatus().name());
        after.put("compensationDays", days);
        auditRecorder.record(
                "APPEAL_COMPENSATE",
                "APPEAL_TICKET",
                id,
                "申诉补偿",
                Map.of("days", days),
                before,
                after);
        return AppealDtos.AppealActionView.of(ticket);
    }

    /**
     * SLA 扫描：每分钟一次，超时未首次回应的工单自动补 3 天。
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)

    /**
     * SLA 巡检：对超时未回应的工单自动补偿，并写 outbox 与审计。
     */
    @Transactional
    public void scanSla() {
        LocalDateTime now = LocalDateTime.now();
        int compensationDays = ruleService.intValue("APPEAL.AUTO_COMP_DAYS", 3);
        List<AppealTicket> due;
        try {
            due = repository.findSlaDue(now, 50);
        } catch (Exception e) {
            log.warn("SLA 扫描失败: {}", e.getMessage());
            return;
        }
        for (AppealTicket ticket : due) {
            if (!ticket.breachSla(now, compensationDays)) {
                continue;
            }
            persist(ticket);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ticketId", ticket.getId());
            payload.put("readerId", ticket.getReaderId());
            payload.put("compensationDays", ticket.getCompensationDays());
            outboxAppender.append("APPEAL_TICKET", ticket.getId(), MqConstant.EVENT_APPEAL_SLA_BREACHED, payload);
            auditRecorder.recordAs(
                    null,
                    "APPEAL_SLA_BREACH",
                    "APPEAL_TICKET",
                    ticket.getId(),
                    "SLA 超时自动补偿",
                    Map.of("compensationDays", compensationDays));
        }
    }

    /**
     * 创建申诉：校验对象归属，按配置计算 SLA 截止时间。
     *
     * @param request 申诉类型、对象 ID 与理由
     * @return 工单视图
     */
    @Transactional
    public AppealDtos.MyTicketView create(AppealDtos.CreateRequest request) {
        Long readerId = UserContext.requireId();
        String targetType = request.targetType().trim().toUpperCase();
        if (!TARGET_TYPES.contains(targetType)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR, "申诉对象类型只能是 GRACE/OVERDUE_TIER/RENEW/REMINDER/OTHER");
        }
        validateTarget(targetType, request.targetId(), readerId);
        LocalDateTime now = LocalDateTime.now();
        int slaHours = ruleService.intValue("APPEAL.SLA_HOURS", 2);
        AppealTicket ticket = new AppealTicket(
                null,
                nextTicketNo(now),
                readerId,
                targetType,
                request.targetId(),
                request.reason().trim(),
                AppealStatus.OPEN,
                now.plusHours(slaHours),
                null,
                false,
                null,
                null,
                0,
                null,
                now);
        Long id = repository.insert(ticket);
        auditRecorder.record(
                "APPEAL_CREATE",
                "APPEAL_TICKET",
                id,
                "读者提交申诉",
                Map.of("targetType", targetType, "targetId", request.targetId()));
        return new AppealDtos.MyTicketView(
                id,
                ticket.getTicketNo(),
                targetType,
                request.targetId(),
                ticket.getReason(),
                AppealStatus.OPEN.name(),
                Times.iso(now),
                Times.iso(ticket.getSlaDueAt()),
                0,
                null);
    }

    /**
     * 分页查询当前读者的工单。
     *
     * @param status    状态过滤
     * @param pageQuery 分页参数
     * @return 工单分页
     */
    @Transactional(readOnly = true)
    public PageResult<AppealDtos.MyTicketView> myTickets(String status, PageQuery pageQuery) {
        Long readerId = UserContext.requireId();
        List<AppealTicket> tickets =
                repository.findByReader(readerId, status, (int) pageQuery.offset(), pageQuery.size());
        long total = repository.countByReader(readerId, status);
        return PageResult.of(tickets.stream().map(AppealDtos.MyTicketView::of).toList(), total, pageQuery);
    }

    private void validateTarget(String targetType, Long targetId, Long readerId) {
        switch (targetType) {
            case "GRACE" -> graceService.requireOwned(targetId, readerId);
            case "RENEW", "OVERDUE_TIER" -> {
                if (!readerId.equals(borrowService.requireOrder(targetId).getReaderId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只能申诉自己的借阅");
                }
            }
            default -> {
                // REMINDER / OTHER 不绑定具体业务对象，无需归属校验
            }
        }
    }

    private String nextTicketNo(LocalDateTime now) {
        int suffix = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 10000);
        return "AP" + now.format(TICKET_NO_DATE) + suffix;
    }

    private void persist(AppealTicket ticket) {
        if (repository.update(ticket) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "工单已被其他人处理，请刷新后重试");
        }
    }

    private AppealTicket require(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申诉工单不存在"));
    }

    private String note(AppealDtos.ActionRequest request) {
        return request == null || request.note() == null ? "" : request.note().trim();
    }
}
