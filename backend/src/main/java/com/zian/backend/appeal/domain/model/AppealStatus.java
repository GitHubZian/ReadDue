package com.zian.backend.appeal.domain.model;

/**
 * 申诉工单状态。
 */
public enum AppealStatus {
    OPEN,
    ACKED,
    RESOLVED,
    REJECTED,
    AUTO_COMPENSATED,
    CLOSED;

    public boolean isOpen() {
        return this == OPEN || this == ACKED;
    }
}
