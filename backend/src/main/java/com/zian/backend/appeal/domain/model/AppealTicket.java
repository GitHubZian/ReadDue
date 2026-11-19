package com.zian.backend.appeal.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 申诉工单聚合：SLA 2 小时内必须首次回应，超时自动补偿 3 天。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealTicket {

    private Long id;
    private String ticketNo;
    private Long readerId;
    private String targetType;
    private Long targetId;
    private String reason;
    private AppealStatus status;
    private LocalDateTime slaDueAt;
    private LocalDateTime firstReplyAt;
    private boolean slaBreached;
    private Long assigneeId;
    private String assigneeName;
    private int compensationDays;
    private String resolution;
    private LocalDateTime createdAt;

    /**
     * 受理：OPEN → ACKED 并记录首次回应时间。
     *
     * @param operatorId   受理人 ID
     * @param operatorName 受理人姓名
     * @param now          受理时间
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
     *
     * @param note         结案说明
     * @param operatorId   处理人 ID
     * @param operatorName 处理人姓名
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
     *
     * @param note         驳回说明
     * @param operatorId   处理人 ID
     * @param operatorName 处理人姓名
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
     *
     * @param days         本次补偿天数
     * @param note         处理说明
     * @param operatorId   处理人 ID
     * @param operatorName 处理人姓名
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
     *
     * @param now               当前时间
     * @param compensationDays  自动补偿天数
     * @return 是否触发了自动补偿
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
