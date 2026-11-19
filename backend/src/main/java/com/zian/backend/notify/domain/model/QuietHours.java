package com.zian.backend.notify.domain.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 读者免打扰时段（单窗口）。
 *
 * <p>{@code start} 晚于 {@code end} 表示跨零点，例如 22:00–08:00；窗口按 {@code [start, end)} 计算，
 * 命中时把计划时间顺延到窗口结束。
 *
 * @param enabled    是否启用
 * @param start      开始时间
 * @param end        结束时间（跨零点时早于开始）
 * @param customized 是否读者自定义过（false 表示全局兜底）
 */
public record QuietHours(boolean enabled, LocalTime start, LocalTime end, boolean customized) {

    /**
     * 判断某个时刻是否落在窗口内。
     *
     * @param time 待判断时刻
     * @return 命中返回 true；未启用或起止相同返回 false
     */
    public boolean covers(LocalTime time) {
        if (!enabled || start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        return !time.isBefore(start) || time.isBefore(end);
    }

    /**
     * 命中窗口时把计划时间平移到窗口结束。
     *
     * @param at 原计划时间
     * @return 平移后的时间；未命中返回原值
     */
    public LocalDateTime shift(LocalDateTime at) {
        if (!covers(at.toLocalTime())) {
            return at;
        }
        boolean nextDay = !start.isBefore(end) && !at.toLocalTime().isBefore(start);
        return (nextDay ? at.toLocalDate().plusDays(1) : at.toLocalDate()).atTime(end);
    }

    /**
     * 使用全局兜底窗口。
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 未自定义的偏好
     */
    public static QuietHours fallback(LocalTime start, LocalTime end) {
        return new QuietHours(true, start, end, false);
    }
}
