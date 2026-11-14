package com.zian.backend.notify.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.notify.domain.repository.ChannelPrefRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.ReaderChannelPrefMapper;
import com.zian.backend.notify.infrastructure.persistence.po.ReaderChannelPrefPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 渠道偏好读取：EMAIL 行存在则以该行为准，缺省视为开启。
 */
@Repository
@RequiredArgsConstructor
public class ChannelPrefRepositoryImpl implements ChannelPrefRepository {

    private static final String CHANNEL_EMAIL = "EMAIL";

    private final ReaderChannelPrefMapper mapper;

    @Override
    public boolean emailEnabled(Long userId) {
        ReaderChannelPrefPO pref =
                mapper.selectOne(
                        Wrappers.<ReaderChannelPrefPO>lambdaQuery()
                                .eq(ReaderChannelPrefPO::getUserId, userId)
                                .eq(ReaderChannelPrefPO::getChannel, CHANNEL_EMAIL)
                                .last("limit 1"));
        return pref == null || Integer.valueOf(1).equals(pref.getEnabled());
    }
}
