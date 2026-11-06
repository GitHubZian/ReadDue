package com.zian.backend.appeal.application;

import com.zian.backend.appeal.domain.model.AppealTicket;
import com.zian.backend.appeal.domain.repository.AppealTicketRepository;
import com.zian.backend.appeal.interfaces.dto.AppealDtos;
import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final AppealTicketRepository repository;
    private final RuleService ruleService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;

    @Transactional
    public AppealDtos.AppealActionView ack(Long id) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        ticket.ack(operator.id(), operator.name(), LocalDateTime.now());
        persist(ticket);
        auditRecorder.record("APPEAL_ACK", "APPEAL_TICKET", id, "申诉受理", Map.of());
        return AppealDtos.AppealActionView.of(ticket);
    }

    @Transactional
    public AppealDtos.AppealActionView resolve(Long id, AppealDtos.ActionRequest request) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        ticket.resolve(note(request), operator.id(), operator.name());
        persist(ticket);
        auditRecorder.record("APPEAL_RESOLVE", "APPEAL_TICKET", id, "申诉处理", Map.of("note", note(request)));
        return AppealDtos.AppealActionView.of(ticket);
    }

    @Transactional
    public AppealDtos.AppealActionView reject(Long id, AppealDtos.ActionRequest request) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        ticket.reject(note(request), operator.id(), operator.name());
        persist(ticket);
        auditRecorder.record("APPEAL_REJECT", "APPEAL_TICKET", id, "申诉处理", Map.of("note", note(request)));
        return AppealDtos.AppealActionView.of(ticket);
    }

    @Transactional
    public AppealDtos.AppealActionView compensate(Long id, AppealDtos.ActionRequest request) {
        LoginUser operator = UserContext.require();
        AppealTicket ticket = require(id);
        int days = request != null && request.days() != null ? request.days() : 3;
        ticket.compensate(days, note(request), operator.id(), operator.name());
        persist(ticket);
        auditRecorder.record("APPEAL_COMPENSATE", "APPEAL_TICKET", id, "申诉补偿", Map.of("days", days));
        return AppealDtos.AppealActionView.of(ticket);
    }

    /**
     * SLA 扫描：每分钟一次，超时未首次回应的工单自动补 3 天。
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
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
