package com.zian.backend.rule.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.rule.domain.model.AcademicCalendar;
import com.zian.backend.rule.domain.repository.AcademicCalendarRepository;
import com.zian.backend.rule.infrastructure.persistence.mapper.AcademicCalendarMapper;
import com.zian.backend.rule.infrastructure.persistence.po.AcademicCalendarPO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 校历仓储实现：逻辑删除列 {@code deleted} 存 0 或行主键。
 */
@Repository
@RequiredArgsConstructor
public class AcademicCalendarRepositoryImpl implements AcademicCalendarRepository {

    private final AcademicCalendarMapper mapper;

    @Override
    public List<AcademicCalendar> findAll() {
        return mapper.selectList(Wrappers.<AcademicCalendarPO>lambdaQuery()
                        .eq(AcademicCalendarPO::getDeleted, 0L)
                        .orderByDesc(AcademicCalendarPO::getStartDate))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<AcademicCalendar> findById(long id) {
        return mapper.selectList(Wrappers.<AcademicCalendarPO>lambdaQuery()
                        .eq(AcademicCalendarPO::getId, id)
                        .eq(AcademicCalendarPO::getDeleted, 0L))
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<AcademicCalendar> findOverlaps(
            String term, String sceneCode, LocalDate start, LocalDate end, Long excludeId) {
        var query = Wrappers.<AcademicCalendarPO>lambdaQuery()
                .eq(AcademicCalendarPO::getTerm, term)
                .eq(AcademicCalendarPO::getSceneCode, sceneCode)
                .eq(AcademicCalendarPO::getDeleted, 0L)
                .le(AcademicCalendarPO::getStartDate, end)
                .ge(AcademicCalendarPO::getEndDate, start);
        if (excludeId != null) {
            query.ne(AcademicCalendarPO::getId, excludeId);
        }
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public long insert(AcademicCalendar calendar) {
        AcademicCalendarPO po = new AcademicCalendarPO();
        po.setTerm(calendar.term());
        po.setSceneCode(calendar.sceneCode());
        po.setStartDate(calendar.startDate());
        po.setEndDate(calendar.endDate());
        po.setSource(calendar.source());
        po.setNote(calendar.note());
        po.setDeleted(0L);
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public void update(AcademicCalendar calendar) {
        AcademicCalendarPO po = new AcademicCalendarPO();
        po.setId(calendar.id());
        po.setTerm(calendar.term());
        po.setSceneCode(calendar.sceneCode());
        po.setStartDate(calendar.startDate());
        po.setEndDate(calendar.endDate());
        po.setSource(calendar.source());
        po.setNote(calendar.note());
        mapper.updateById(po);
    }

    @Override
    public void softDelete(long id, long operatorId) {
        mapper.softDelete(id, operatorId);
    }

    private AcademicCalendar toDomain(AcademicCalendarPO po) {
        return new AcademicCalendar(
                po.getId(),
                po.getTerm(),
                po.getSceneCode(),
                po.getStartDate(),
                po.getEndDate(),
                po.getSource(),
                po.getNote());
    }
}
