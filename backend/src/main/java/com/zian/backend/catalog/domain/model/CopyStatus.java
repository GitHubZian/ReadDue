package com.zian.backend.catalog.domain.model;

/**
 * 馆藏副本状态（与库内 book_copy.status 一致）。
 */
public enum CopyStatus {
    AVAILABLE,
    ON_LOAN,
    READY_SHELF,
    LOST,
    DAMAGED,
    REPAIRING,
    WITHDRAWN
}
