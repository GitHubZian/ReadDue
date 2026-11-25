package com.zian.backend.iam.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.iam.domain.model.Course;
import com.zian.backend.iam.domain.repository.TimetableRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.TimetableMapper;
import com.zian.backend.iam.infrastructure.persistence.po.TimetablePO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读者课表仓储实现（MyBatis-Plus，M5-F）。
 *
 * <p>覆盖式保存：旧记录软删除后写入新记录，历史数据保留以便审计。
 */
@Repository
@RequiredArgsConstructor
public class TimetableRepositoryImpl implements TimetableRepository {

    private final TimetableMapper mapper;

    @Override
    public List<Course> findActive(long userId) {
        return mapper.selectList(Wrappers.<TimetablePO>lambdaQuery()
                        .eq(TimetablePO::getUserId, userId)
                        .eq(TimetablePO::getDeleted, 0)
                        .orderByAsc(TimetablePO::getWeekday)
                        .orderByAsc(TimetablePO::getStartTime))
                .stream()
                .map(po -> new Course(po.getWeekday(), po.getStartTime(), po.getEndTime()))
                .toList();
    }

    @Override
    public void replace(long userId, List<Course> courses) {
        LocalDateTime now = LocalDateTime.now();
        mapper.update(null, Wrappers.<TimetablePO>lambdaUpdate()
                .eq(TimetablePO::getUserId, userId)
                .eq(TimetablePO::getDeleted, 0)
                .set(TimetablePO::getDeleted, 1)
                .set(TimetablePO::getUpdatedAt, now));
        for (Course course : courses) {
            TimetablePO po = new TimetablePO();
            po.setUserId(userId);
            po.setWeekday(course.weekday());
            po.setStartTime(course.startTime());
            po.setEndTime(course.endTime());
            po.setDeleted(0);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            mapper.insert(po);
        }
    }

    @Override
    public LocalDateTime lastUpdated(long userId) {
        return mapper.selectList(Wrappers.<TimetablePO>lambdaQuery()
                        .eq(TimetablePO::getUserId, userId)
                        .eq(TimetablePO::getDeleted, 0))
                .stream()
                .map(TimetablePO::getUpdatedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
