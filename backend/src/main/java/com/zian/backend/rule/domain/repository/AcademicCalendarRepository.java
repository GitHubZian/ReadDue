package com.zian.backend.rule.domain.repository;

import com.zian.backend.rule.domain.model.AcademicCalendar;
import java.util.List;
import java.util.Optional;

/**
 * 校历区间仓储。
 */
public interface AcademicCalendarRepository {

    /**
     * 全部未删除区间，按开始日期倒序。
     *
     * @return 校历列表
     */
    List<AcademicCalendar> findAll();

    /**
     * 按主键查未删除区间。
     *
     * @param id 主键
     * @return 不存在或已删除时为空
     */
    Optional<AcademicCalendar> findById(long id);

    /**
     * 同学期、同场景下与给定区间重叠的记录（可排除自身）。
     *
     * @param term      学期
     * @param sceneCode 场景码
     * @param start     开始（含）
     * @param end       结束（含）
     * @param excludeId 需要排除的主键，新建时传 null
     * @return 重叠记录
     */
    List<AcademicCalendar> findOverlaps(String term, String sceneCode, java.time.LocalDate start, java.time.LocalDate end, Long excludeId);

    /**
     * 新建区间。
     *
     * @param calendar 待插入记录，id 为空
     * @return 生成的主键
     */
    long insert(AcademicCalendar calendar);

    /**
     * 全量更新区间（term/scene/日期/source/note）。
     *
     * @param calendar 含 id 的记录
     */
    void update(AcademicCalendar calendar);

    /**
     * 软删除。
     *
     * @param id         主键
     * @param operatorId 操作者 ID，写入 deleted_by
     */
    void softDelete(long id, long operatorId);
}
