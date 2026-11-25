package com.zian.backend.iam.domain.model;

/**
 * 账号状态。
 *
 * <p>拉黑只限制「借出 / 续借 / 预约 / 申请宽限」，归还与申诉必须始终可用（AGENT.md 硬约束）。
 */
public enum UserStatus {
    NORMAL,
    BLACKLISTED,
    /** 已注销：不可登录，既有令牌立即失效。 */
    DISABLED
}
