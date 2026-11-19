package com.zian.backend.iam.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号聚合根。
 *
 * <p>充血职责：状态机（NORMAL ⇄ BLACKLISTED）、借阅准入判定、登录时间戳。
 * 状态变更必须走本类方法，禁止在 application 层直接 setStatus。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String userNo;
    private String name;
    private UserRole role;
    private String phone;
    private String email;
    private String passwordHash;
    private UserStatus status;
    private String blacklistReason;
    private LocalDateTime blacklistedAt;
    private Long blacklistedBy;
    private LocalDateTime blacklistExpiresAt;
    private LocalDateTime lastLoginAt;
    private Integer version;
    private LocalDateTime createdAt;

    /**
     * 是否处于拉黑状态。
     */
    public boolean isBlacklisted() {
        return status == UserStatus.BLACKLISTED;
    }

    /**
     * 是否管理员。
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * 校验账号可执行借阅类操作，拉黑时抛 422。
     *
     * @param action 操作名称，用于拼接提示
     */
    public void assertCanBorrow(String action) {
        if (isBlacklisted()) {
            String reason = blacklistReason == null ? "未填写原因" : blacklistReason;
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "账号已被限制，" + action + "不可用：" + reason);
        }
    }

    /**
     * 拉黑账号：校验原因与操作人，记录到期时间。
     *
     * @param reason     拉黑原因
     * @param operatorId 操作人，等于本人时拒绝
     * @param expiresAt  自动解除时间，可空
     */
    public void blacklist(String reason, Long operatorId, LocalDateTime expiresAt) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "拉黑必须填写原因");
        }
        if (operatorId != null && operatorId.equals(this.id)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "不能拉黑自己");
        }
        this.status = UserStatus.BLACKLISTED;
        this.blacklistReason = reason;
        this.blacklistedAt = LocalDateTime.now();
        this.blacklistedBy = operatorId;
        this.blacklistExpiresAt = expiresAt;
    }

    /**
     * 解除拉黑并清空原因与到期时间。
     */
    public void restore() {
        if (!isBlacklisted()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "账号当前不是限制状态");
        }
        this.status = UserStatus.NORMAL;
        this.blacklistReason = null;
        this.blacklistedAt = null;
        this.blacklistedBy = null;
        this.blacklistExpiresAt = null;
    }

    /**
     * 记录最近登录时间。
     *
     * @param at 登录时间
     */
    public void touchLogin(LocalDateTime at) {
        this.lastLoginAt = at;
    }
}
