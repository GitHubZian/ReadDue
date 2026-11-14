package com.zian.backend.appeal.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 申诉工单聚合：SLA 2 小时内必须首次回应，超时自动补偿 3 天。
 */
public class AppealTicket {

    private final Long id;
    private final String ticketNo;
    private final Long readerId;
    private final String targetType;
    private final Long targetId;
    private final String reason;
    private AppealStatus status;
    private final LocalDateTime slaDueAt;
    private LocalDateTime firstReplyAt;
    private boolean slaBreached;
    private Long assigneeId;
    private String assigneeName;
    private int compensationDays;
    private String resolution;
    private final LocalDateTime createdAt;

    public AppealTicket(
            Long id,
            String ticketNo,
            Long readerId,
            String targetType,
            Long targetId,
            String reason,
            AppealStatus status,
            LocalDateTime slaDueAt,
            LocalDateTime firstReplyAt,
            boolean slaBreached,
            Long assigneeId,
            String assigneeName,
            int compensationDays,
            String resolution,
            LocalDateTime createdAt) {
        this.id = id;
        this.ticketNo = ticketNo;
        this.readerId = readerId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = status;
        this.slaDueAt = slaDueAt;
        this.firstReplyAt = firstReplyAt;
        this.slaBreached = slaBreached;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.compensationDays = compensationDays;
        this.resolution = resolution;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public Long getReaderId() {
        return readerId;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public AppealStatus getStatus() {
        return status;
    }

    public LocalDateTime getSlaDueAt() {
        return slaDueAt;
    }

    public LocalDateTime getFirstReplyAt() {
        return firstReplyAt;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public int getCompensationDays() {
        return compensationDays;
    }

    public String getResolution() {
        return resolution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 受理：OPEN → ACKED 并记录首次回应时间。
     */
    public void ack(Long operatorId, String operatorName, LocalDateTime now) {
        if (!status.isOpen()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该工单已结束，无法受理");
        }
        if (status == AppealStatus.OPEN) {
            this.status = AppealStatus.ACKED;
        }
        this.assigneeId = operatorId;
        this.assigneeName = operatorName;
        if (this.firstReplyAt == null) {
            this.firstReplyAt = now;
        }
    }

    /**
     * 出结论：置为 RESOLVED 并记录说明。
     */
    public void resolve(String note, Long operatorId, String operatorName) {
        requireOpen();
        this.status = AppealStatus.RESOLVED;
        this.resolution = note;
        this.assigneeId = operatorId;
        this.assigneeName = operatorName;
    }

    /**
     * 驳回：置为 REJECTED。
     */
    public void reject(String note, Long operatorId, String operatorName) {
        requireOpen();
        this.status = AppealStatus.REJECTED;
        this.resolution = note;
        this.assigneeId = operatorId;
        this.assigneeName = operatorName;
    }

    /**
     * 补偿：置为 AUTO_COMPENSATED 并累加补偿天数。
     */
    public void compensate(int days, String note, Long operatorId, String operatorName) {
        requireOpen();
        if (days <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "补偿天数必须大于 0");
        }
        this.compensationDays += days;
        this.status = AppealStatus.AUTO_COMPENSATED;
        this.resolution = note;
        this.assigneeId = operatorId;
        this.assigneeName = operatorName;
    }

    /**
     * SLA 超时处理：超时且未补偿时自动补偿，返回是否发生。
     */
    public boolean breachSla(LocalDateTime now, int compensationDays) {
        if (!status.isOpen() || slaBreached) {
            return false;
        }
        if (now.isBefore(slaDueAt)) {
            return false;
        }
        this.slaBreached = true;
        this.compensationDays += compensationDays;
        this.status = AppealStatus.AUTO_COMPENSATED;
        this.resolution = "SLA 超时（" + ChronoUnit.MINUTES.between(slaDueAt, now) + " 分钟）自动补偿 "
                + compensationDays + " 天";
        return true;
    }

    private void requireOpen() {
        if (!status.isOpen()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该工单已结束，无法重复处理");
        }
    }
}
