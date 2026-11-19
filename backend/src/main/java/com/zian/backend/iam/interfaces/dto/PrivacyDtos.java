package com.zian.backend.iam.interfaces.dto;

/**
 * 读者隐私接口的请求与响应体。
 */
public final class PrivacyDtos {

    private PrivacyDtos() {}

    /**
     * 授权状态视图。
     *
     * @param sceneConsent 场景声明授权
     * @param cacheConsent 宽限计算缓存授权
     * @param consentRevokedAt 最近一次关闭授权时间（`+08:00`，从未关闭为 null）
     * @param forgotten 是否已提交过遗忘
     */
    public record ConsentsView(
            boolean sceneConsent, boolean cacheConsent, String consentRevokedAt, boolean forgotten) {}

    /**
     * 局部更新授权。
     *
     * @param sceneConsent 场景声明授权，null 表示不修改
     * @param cacheConsent 缓存授权，null 表示不修改
     */
    public record UpdateRequest(Boolean sceneConsent, Boolean cacheConsent) {}

    /**
     * 遗忘结果。
     *
     * @param forgotten 是否已脱敏
     * @param displayName 脱敏后的展示名
     * @param forgetRequestedAt 遗忘时间（`+08:00`）
     */
    public record ForgetResult(boolean forgotten, String displayName, String forgetRequestedAt) {}
}
