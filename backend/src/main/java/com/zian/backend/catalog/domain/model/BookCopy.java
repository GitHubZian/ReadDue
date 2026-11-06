package com.zian.backend.catalog.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;

/**
 * 馆藏副本聚合：负责副本在「可借 / 在借 / 待取 / 损坏」之间的状态迁移。
 */
public class BookCopy {

    private final Long id;
    private final Long bookId;
    private final String barcode;
    private final String location;
    private CopyStatus status;
    private final Integer version;

    public BookCopy(Long id, Long bookId, String barcode, String location, CopyStatus status, Integer version) {
        this.id = id;
        this.bookId = bookId;
        this.barcode = barcode;
        this.location = location;
        this.status = status;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLocation() {
        return location;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isLendable() {
        return status == CopyStatus.AVAILABLE;
    }

    /** 借出：仅在架可借副本才能出借。 */
    public void lend() {
        if (status != CopyStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该副本当前状态为「" + status + "」，不可借出");
        }
        this.status = CopyStatus.ON_LOAN;
    }

    /** 归还：正常回架，损坏转 DAMAGED 等修复。 */
    public void receive(boolean damaged) {
        if (status != CopyStatus.ON_LOAN) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该副本不在借出状态，无法归还");
        }
        this.status = damaged ? CopyStatus.DAMAGED : CopyStatus.AVAILABLE;
    }

    /** 预约到书：预留在架，等待读者取书。 */
    public void holdForReservation() {
        if (status != CopyStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该副本当前不可预留在架");
        }
        this.status = CopyStatus.READY_SHELF;
    }

    /** 释放预留在架（读者取走或预约过期）。 */
    public void releaseHold() {
        if (status == CopyStatus.READY_SHELF) {
            this.status = CopyStatus.AVAILABLE;
        }
    }
}
