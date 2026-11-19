package com.zian.backend.grace.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宽限申请聚合：审批只能发生在 PENDING，批准天数受 Dmax 约束。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraceApplication {

    private Long id;
    private String applyNo;
    private Long borrowOrderId;
    private Long readerId;
    private String sceneCode;
    private String sceneSource;
    private boolean calendarHit;
    private BigDecimal baseDays;
    private BigDecimal kt;
    private BigDecimal kq;
    private int dmax;
    private BigDecimal computedDays;
    private Integer grantedDays;
    private String evidenceText;
    private String formulaVersion;
    private int queueLen;
    private LocalDateTime snapshotExpiresAt;
    private GraceStatus status;
    private Long decidedBy;
    private LocalDateTime decidedAt;
    private String rejectCode;
    private Integer version;

    /**
     * 是否仍在待审批状态。
     */
    public boolean isPending() {
        return status == GraceStatus.PENDING;
    }

    /**
     * 审批通过：校验待审状态与天数上限。
     *
     * @param days       批准天数
     * @param operatorId 审批人
     * @param now        审批时间
     */
    public void approve(int days, Long operatorId, LocalDateTime now) {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该申请已处理，无法重复审批");
        }
        if (days <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "批准天数必须大于 0");
        }
        if (days > dmax) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "批准天数不能超过封顶 " + dmax + " 天");
        }
        this.status = GraceStatus.APPROVED;
        this.grantedDays = days;
        this.decidedBy = operatorId;
        this.decidedAt = now;
    }

    /**
     * 驳回：记录原因码与审批人。
     *
     * @param code       驳回原因码，空则回退 NOT_ELIGIBLE
     * @param operatorId 审批人
     * @param now        审批时间
     */
    public void reject(String code, Long operatorId, LocalDateTime now) {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该申请已处理，无法重复审批");
        }
        this.status = GraceStatus.REJECTED;
        this.rejectCode = code == null || code.isBlank() ? "NOT_ELIGIBLE" : code;
        this.decidedBy = operatorId;
        this.decidedAt = now;
    }
}
