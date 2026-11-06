package com.zian.backend.grace.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.application.BorrowService;
import com.zian.backend.borrow.domain.model.BorrowOrder;
import com.zian.backend.grace.domain.model.GraceApplication;
import com.zian.backend.grace.domain.repository.GraceApplicationRepository;
import com.zian.backend.grace.interfaces.dto.GraceDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宽限审批应用服务：批准后同时延长借阅单有效应还日期，并发出重排提醒事件。
 */
@Service
@RequiredArgsConstructor
public class GraceService {

    private final GraceApplicationRepository repository;
    private final BorrowService borrowService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;

    @Transactional
    public GraceDtos.GraceDecisionView approve(Long id, GraceDtos.ApproveRequest request) {
        Long operatorId = UserContext.requireId();
        LocalDateTime now = LocalDateTime.now();
        GraceApplication application = require(id);
        int days = request != null && request.days() != null
                ? request.days()
                : com.zian.backend.grace.domain.model.GraceCalculator.asDays(application.getComputedDays());
        application.approve(days, operatorId, now);
        if (repository.update(application) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "申请已被其他人处理，请刷新后重试");
        }

        BorrowOrder order = borrowService.applyGrace(application.getBorrowOrderId(), days);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("graceId", application.getId());
        payload.put("orderId", order.getId());
        payload.put("readerId", application.getReaderId());
        payload.put("days", days);
        payload.put("effectiveDueAt", String.valueOf(order.getEffectiveDueAt()));
        outboxAppender.append("GRACE_APPLICATION", application.getId(), MqConstant.EVENT_GRACE_APPROVED, payload);
        auditRecorder.record(
                "GRACE_APPROVE",
                "GRACE_APPLICATION",
                id,
                "宽限审批",
                Map.of("days", days, "note", request == null || request.note() == null ? "" : request.note()));
        return GraceDtos.GraceDecisionView.of(application);
    }

    @Transactional
    public GraceDtos.GraceDecisionView reject(Long id, GraceDtos.RejectRequest request) {
        Long operatorId = UserContext.requireId();
        GraceApplication application = require(id);
        String note = request == null || request.note() == null ? "" : request.note().trim();
        application.reject("REJECTED", operatorId, LocalDateTime.now());
        if (repository.update(application) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "申请已被其他人处理，请刷新后重试");
        }
        auditRecorder.record("GRACE_REJECT", "GRACE_APPLICATION", id, "宽限审批", Map.of("note", note));
        return GraceDtos.GraceDecisionView.of(application);
    }

    private GraceApplication require(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "宽限申请不存在"));
    }
}
