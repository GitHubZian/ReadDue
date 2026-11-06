package com.zian.backend.grace.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宽限申请聚合：审批只能发生在 PENDING，批准天数受 Dmax 约束。
 */
public class GraceApplication {

    private final Long id;
    private final String applyNo;
    private final Long borrowOrderId;
    private final Long readerId;
    private final String sceneCode;
    private final String sceneSource;
    private final boolean calendarHit;
    private final BigDecimal baseDays;
    private final BigDecimal kt;
    private final BigDecimal kq;
    private final int dmax;
    private final BigDecimal computedDays;
    private Integer grantedDays;
    private final String evidenceText;
    private final String formulaVersion;
    private final int queueLen;
    private final LocalDateTime snapshotExpiresAt;
    private GraceStatus status;
    private Long decidedBy;
    private LocalDateTime decidedAt;
    private String rejectCode;
    private final Integer version;

    public GraceApplication(
            Long id,
            String applyNo,
            Long borrowOrderId,
            Long readerId,
            String sceneCode,
            String sceneSource,
            boolean calendarHit,
            BigDecimal baseDays,
            BigDecimal kt,
            BigDecimal kq,
            int dmax,
            BigDecimal computedDays,
            Integer grantedDays,
            String evidenceText,
            String formulaVersion,
            int queueLen,
            LocalDateTime snapshotExpiresAt,
            GraceStatus status,
            Long decidedBy,
            LocalDateTime decidedAt,
            String rejectCode,
            Integer version) {
        this.id = id;
        this.applyNo = applyNo;
        this.borrowOrderId = borrowOrderId;
        this.readerId = readerId;
        this.sceneCode = sceneCode;
        this.sceneSource = sceneSource;
        this.calendarHit = calendarHit;
        this.baseDays = baseDays;
        this.kt = kt;
        this.kq = kq;
        this.dmax = dmax;
        this.computedDays = computedDays;
        this.grantedDays = grantedDays;
        this.evidenceText = evidenceText;
        this.formulaVersion = formulaVersion;
        this.queueLen = queueLen;
        this.snapshotExpiresAt = snapshotExpiresAt;
        this.status = status;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
        this.rejectCode = rejectCode;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public Long getBorrowOrderId() {
        return borrowOrderId;
    }

    public Long getReaderId() {
        return readerId;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public String getSceneSource() {
        return sceneSource;
    }

    public boolean isCalendarHit() {
        return calendarHit;
    }

    public BigDecimal getBaseDays() {
        return baseDays;
    }

    public BigDecimal getKt() {
        return kt;
    }

    public BigDecimal getKq() {
        return kq;
    }

    public int getDmax() {
        return dmax;
    }

    public BigDecimal getComputedDays() {
        return computedDays;
    }

    public Integer getGrantedDays() {
        return grantedDays;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public String getFormulaVersion() {
        return formulaVersion;
    }

    public int getQueueLen() {
        return queueLen;
    }

    public LocalDateTime getSnapshotExpiresAt() {
        return snapshotExpiresAt;
    }

    public GraceStatus getStatus() {
        return status;
    }

    public Long getDecidedBy() {
        return decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public String getRejectCode() {
        return rejectCode;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isPending() {
        return status == GraceStatus.PENDING;
    }

    /** 批准：天数必须在 1..Dmax 之间。 */
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
