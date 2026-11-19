package com.zian.backend.rule.domain.model;

import java.time.LocalDate;

/**
 * 校历场景区间：某个学期内某场景（毕业/考试/论文/常规）的起止日期。
 *
 * <p>幂等键为 {@code (term, sceneCode, startDate)}；区间重叠由应用服务校验。
 *
 * @param id        主键
 * @param term      学期，如 2026-FALL
 * @param sceneCode 场景码 GRADUATION/EXAM/THESIS/NORMAL
 * @param startDate 开始日期（含）
 * @param endDate   结束日期（含）
 * @param source    来源 CALENDAR（教务导入）/DECLARED（管理端手工）
 * @param note      备注
 */
public record AcademicCalendar(
        Long id,
        String term,
        String sceneCode,
        LocalDate startDate,
        LocalDate endDate,
        String source,
        String note) {

    /** 同一学期同一场景，区间是否有交集（闭区间）。 */
    public boolean overlaps(LocalDate start, LocalDate end) {
        return !startDate.isAfter(end) && !start.isAfter(endDate);
    }
}
