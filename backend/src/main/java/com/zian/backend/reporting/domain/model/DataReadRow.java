package com.zian.backend.reporting.domain.model;

import java.time.LocalDateTime;

/**
 * 敏感数据读取记录：来自 audit_log 中 action = DATA_READ 的行。
 *
 * @param id           审计日志主键
 * @param operatorId   操作者账号 ID
 * @param operatorName 操作者姓名
 * @param targetType   目标类型：USER/GRACE
 * @param targetId     目标 ID
 * @param createdAt    读取时间
 */
public record DataReadRow(
        Long id,
        Long operatorId,
        String operatorName,
        String targetType,
        Long targetId,
        LocalDateTime createdAt) {
}
