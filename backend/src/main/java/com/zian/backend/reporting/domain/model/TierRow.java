package com.zian.backend.reporting.domain.model;

/**
 * 逾期分档行：T1–T4 的区间、动作、话术与是否可续借。
 *
 * @param code        分档码
 * @param name        分档名称
 * @param minDays     起始逾期天数
 * @param maxDays     结束逾期天数；null 表示无上限
 * @param actionCode  处置动作码
 * @param toneCode    话术语气码
 * @param allowRenew  该档位是否仍可续借
 * @param description 对读者公示的说明
 */
public record TierRow(
        String code,
        String name,
        Integer minDays,
        Integer maxDays,
        String actionCode,
        String toneCode,
        Boolean allowRenew,
        String description) {
}
