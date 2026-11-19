package com.zian.backend.iam.domain.model;

import java.time.LocalDateTime;

/**
 * 读者的隐私授权状态。
 *
 * <p>授权默认关闭（最小授权）：关闭「场景声明」时不读取课表等外部数据，关闭「计算缓存」时不把宽限计算快照写入缓存。
 *
 * @param sceneConsent 场景声明授权
 * @param cacheConsent 宽限计算缓存授权
 * @param consentRevokedAt 最近一次关闭授权时间，用于触发立即遗忘
 * @param forgetRequestedAt 读者提交「立即遗忘」的时间；非空表示已完成脱敏
 */
public record PrivacyState(
        boolean sceneConsent,
        boolean cacheConsent,
        LocalDateTime consentRevokedAt,
        LocalDateTime forgetRequestedAt) {

    /**
     * 最小授权：两项都关闭，且从未提交过遗忘。
     *
     * @return 默认状态
     */
    public static PrivacyState minimal() {
        return new PrivacyState(false, false, null, null);
    }

    /**
     * 是否已提交过遗忘请求。
     *
     * @return 已脱敏时为 true
     */
    public boolean forgotten() {
        return forgetRequestedAt != null;
    }
}
