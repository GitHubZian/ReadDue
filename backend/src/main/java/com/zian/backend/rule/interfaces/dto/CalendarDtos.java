package com.zian.backend.rule.interfaces.dto;

import java.time.LocalDate;

/**
 * 校历写接口的请求与响应体。
 *
 * <p>字段名与只读接口 {@code GET /v1/admin/calendar} 对齐，日期统一 {@code yyyy-MM-dd} 字符串。
 */
public final class CalendarDtos {

    private CalendarDtos() {}

    /**
     * 校历区间。
     *
     * @param id        主键（字符串，避免前端精度丢失）
     * @param term      学期，如 2026-FALL
     * @param scene     场景码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param source    来源 CALENDAR/DECLARED
     * @param note      备注
     */
    public record CalendarView(
            String id, String term, String scene, String startDate, String endDate, String source, String note) {}

    /**
     * 新建/更新请求。
     *
     * @param term      学期，如 2026-FALL（必填）
     * @param scene     场景码 GRADUATION/EXAM/THESIS/NORMAL（必填）
     * @param startDate 开始日期（必填）
     * @param endDate   结束日期（必填，不早于开始）
     * @param source    来源，缺省 DECLARED
     * @param note      备注，≤200 字
     */
    public record SaveRequest(
            String term, String scene, LocalDate startDate, LocalDate endDate, String source, String note) {}
}
