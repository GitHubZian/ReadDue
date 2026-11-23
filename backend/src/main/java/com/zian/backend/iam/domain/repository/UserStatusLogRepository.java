package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.UserStatus;
import java.time.LocalDateTime;

/**
 * 账号状态变更日志（{@code user_status_log}，只增不改）。
 */
public interface UserStatusLogRepository {

    /**
     * 追加一条状态变更记录。
     *
     * @param userId       账号 ID
     * @param from         变更前状态
     * @param to           变更后状态
     * @param reason       变更原因
     * @param expiresAt    拉黑期限（仅转 BLACKLISTED 时有值）
     * @param operatorId   操作人；系统任务为 null
     * @param operatorName 操作人名；系统任务为「系统」
     */
    void append(
            Long userId,
            UserStatus from,
            UserStatus to,
            String reason,
            LocalDateTime expiresAt,
            Long operatorId,
            String operatorName);
}
