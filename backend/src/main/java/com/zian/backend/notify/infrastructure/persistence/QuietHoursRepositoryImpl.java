package com.zian.backend.notify.infrastructure.persistence;

import com.zian.backend.notify.domain.model.QuietHours;
import com.zian.backend.notify.domain.repository.QuietHoursRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.ReaderQuietHoursMapper;
import com.zian.backend.notify.infrastructure.persistence.po.ReaderQuietHoursPO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 免打扰时段读写：一行一个读者，无记录由上层回退到全局规则。
 */
@Repository
@RequiredArgsConstructor
public class QuietHoursRepositoryImpl implements QuietHoursRepository {

    private final ReaderQuietHoursMapper mapper;

    @Override
    public Optional<QuietHours> find(long userId) {
        ReaderQuietHoursPO row = mapper.selectById(userId);
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(new QuietHours(
                Integer.valueOf(1).equals(row.getEnabled()),
                row.getStartTime(),
                row.getEndTime(),
                true));
    }

    @Override
    public void save(long userId, QuietHours quietHours) {
        ReaderQuietHoursPO existing = mapper.selectById(userId);
        if (existing == null) {
            ReaderQuietHoursPO row = new ReaderQuietHoursPO();
            row.setUserId(userId);
            row.setEnabled(quietHours.enabled() ? 1 : 0);
            row.setStartTime(quietHours.start());
            row.setEndTime(quietHours.end());
            mapper.insert(row);
            return;
        }
        existing.setEnabled(quietHours.enabled() ? 1 : 0);
        existing.setStartTime(quietHours.start());
        existing.setEndTime(quietHours.end());
        mapper.updateById(existing);
    }
}
