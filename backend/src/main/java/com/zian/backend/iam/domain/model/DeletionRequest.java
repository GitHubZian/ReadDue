package com.zian.backend.iam.domain.model;

import java.time.LocalDateTime;

/**
 * 读者注销申请：先提交申请，管理员批准后复用「立即遗忘」完成脱敏与级联清理。
 *
 * @param id        主键
 * @param userId    申请读者
 * @param status    PENDING/APPROVED/REJECTED
 * @param reason    读者填写的原因
 * @param note      管理员处理备注
 * @param createdAt 提交时间
 * @param handledAt 处理时间；待处理时为 null
 * @param handledBy 处理管理员；待处理时为 null
 */
public record DeletionRequest(
        Long id,
        Long userId,
        String status,
        String reason,
        String note,
        LocalDateTime createdAt,
        LocalDateTime handledAt,
        Long handledBy) {
}
