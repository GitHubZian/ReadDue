package com.zian.backend.reporting.domain.model;

import java.time.LocalDate;

/**
 * 校历行：学期场景区间，用于宽限加成命中判断。
 *
 * @param id        校历主键
 * @param term      学期，如 2026-FALL
 * @param scene     场景码
 * @param startDate 区间开始
 * @param endDate   区间结束
 * @param source    来源（CALENDAR/DECLARED）
 * @param note      备注
 */
public record CalendarRow(
        Long id,
        String term,
        String scene,
        LocalDate startDate,
        LocalDate endDate,
        String source,
        String note) {
}
