package com.zian.backend.borrow.domain.repository;

import java.time.LocalDate;

/**
 * 续借记录仓储：成功与拒绝都留痕，支撑「为什么不能续借」。
 */
public interface RenewRecordRepository {

    /**
     * 记录一次续借尝试（无论通过或拒绝）。
     */

    void append(
            Long borrowOrderId,
            int seq,
            LocalDate oldDueAt,
            LocalDate newDueAt,
            String result,
            String rejectCode,
            int queueLen,
            String operatorType,
            Long operatorId);
}
