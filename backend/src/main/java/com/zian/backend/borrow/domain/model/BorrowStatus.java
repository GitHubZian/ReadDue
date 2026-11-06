package com.zian.backend.borrow.domain.model;

/**
 * 借阅单状态。
 */
public enum BorrowStatus {
    BORROWED,
    OVERDUE,
    RETURNED,
    LOST,
    CLOSED;

    public boolean isActive() {
        return this == BORROWED || this == OVERDUE;
    }
}
