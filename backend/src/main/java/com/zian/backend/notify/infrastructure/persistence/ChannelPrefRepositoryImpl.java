package com.zian.backend.notify.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.notify.domain.model.ChannelPreferences;
import com.zian.backend.notify.domain.repository.ChannelPrefRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.ReaderChannelPrefMapper;
import com.zian.backend.notify.infrastructure.persistence.po.ReaderChannelPrefPO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 渠道偏好读写：按 {@code (user_id, channel)} 唯一键 upsert，缺省视为开启。
 */
@Repository
@RequiredArgsConstructor
public class ChannelPrefRepositoryImpl implements ChannelPrefRepository {

    private static final String CHANNEL_EMAIL = "EMAIL";
    private static final String CHANNEL_INBOX = "INBOX";

    private final ReaderChannelPrefMapper mapper;

    @Override
    public ChannelPreferences load(long userId) {
        List<ReaderChannelPrefPO> rows = mapper.selectList(
                Wrappers.<ReaderChannelPrefPO>lambdaQuery().eq(ReaderChannelPrefPO::getUserId, userId));
        ChannelPreferences defaults = ChannelPreferences.defaults();
        return new ChannelPreferences(
                enabled(rows, CHANNEL_EMAIL, defaults.email()),
                enabled(rows, CHANNEL_INBOX, defaults.inbox()));
    }

    @Override
    public void save(long userId, ChannelPreferences preferences) {
        upsert(userId, CHANNEL_EMAIL, preferences.email());
        upsert(userId, CHANNEL_INBOX, preferences.inbox());
    }

    private boolean enabled(List<ReaderChannelPrefPO> rows, String channel, boolean fallback) {
        return rows.stream()
                .filter(row -> channel.equals(row.getChannel()))
                .findFirst()
                .map(row -> Integer.valueOf(1).equals(row.getEnabled()))
                .orElse(fallback);
    }

    private void upsert(long userId, String channel, boolean enabled) {
        ReaderChannelPrefPO existing = mapper.selectOne(
                Wrappers.<ReaderChannelPrefPO>lambdaQuery()
                        .eq(ReaderChannelPrefPO::getUserId, userId)
                        .eq(ReaderChannelPrefPO::getChannel, channel)
                        .last("limit 1"));
        int flag = enabled ? 1 : 0;
        if (existing == null) {
            ReaderChannelPrefPO row = new ReaderChannelPrefPO();
            row.setId(IdWorker.getId());
            row.setUserId(userId);
            row.setChannel(channel);
            row.setEnabled(flag);
            mapper.insert(row);
            return;
        }
        if (!Integer.valueOf(flag).equals(existing.getEnabled())) {
            existing.setEnabled(flag);
            mapper.updateById(existing);
        }
    }
}
