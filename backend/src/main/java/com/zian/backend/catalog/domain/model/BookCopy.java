package com.zian.backend.catalog.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 馆藏副本聚合：负责副本在「可借 / 在借 / 待取 / 损坏」之间的状态迁移。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {

    private Long id;
    private Long bookId;
    private String barcode;
    private String location;
    private CopyStatus status;
    private Integer version;

    /**
     * 副本是否可借（仅 AVAILABLE）。
     */
    public boolean isLendable() {
        return status == CopyStatus.AVAILABLE;
    }

    /**
     * 借出：AVAILABLE → ON_LOAN，其他状态抛 422。
     */
    public void lend() {
        if (status != CopyStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该副本当前状态为「" + status + "」，不可借出");
        }
        this.status = CopyStatus.ON_LOAN;
    }

    /**
     * 归还：ON_LOAN → AVAILABLE（损坏则 DAMAGED）。
     *
     * @param damaged 归还时是否损坏
     */
    public void receive(boolean damaged) {
        if (status != CopyStatus.ON_LOAN) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该副本不在借出状态，无法归还");
        }
        this.status = damaged ? CopyStatus.DAMAGED : CopyStatus.AVAILABLE;
    }

    /**
     * 为预约锁定副本：AVAILABLE → READY_SHELF。
     */
    public void holdForReservation() {
        if (status != CopyStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该副本当前不可预留在架");
        }
        this.status = CopyStatus.READY_SHELF;
    }

    /**
     * 释放预约占位，回到 AVAILABLE。
     */
    public void releaseHold() {
        if (status == CopyStatus.READY_SHELF) {
            this.status = CopyStatus.AVAILABLE;
        }
    }
}
