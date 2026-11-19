package com.zian.backend.notify.application;

import com.zian.backend.notify.domain.model.ChannelPreferences;
import com.zian.backend.notify.domain.repository.ChannelPrefRepository;
import com.zian.backend.notify.interfaces.dto.ReaderPreferenceDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者自助的渠道开关：读、局部改。
 */
@Service
@RequiredArgsConstructor
public class ReaderPreferenceService {

    private final ChannelPrefRepository channelPrefRepository;

    /**
     * 读取当前读者的渠道开关（未建档返回默认开启）。
     *
     * @return 渠道开关
     */
    public ReaderPreferenceDtos.ChannelsView channels() {
        return view(channelPrefRepository.load(UserContext.requireId()));
    }

    /**
     * 局部更新渠道开关；两个字段都不传则拒绝，避免空请求被当作「全部关闭」。
     *
     * @param request 更新请求
     * @return 更新结果与最新开关
     */
    @Transactional
    public ReaderPreferenceDtos.UpdateResult update(ReaderPreferenceDtos.UpdateRequest request) {
        if (request == null || (request.email() == null && request.inbox() == null)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少指定 email 或 inbox 其中一个");
        }
        long userId = UserContext.requireId();
        ChannelPreferences next = channelPrefRepository.load(userId).merge(request.email(), request.inbox());
        channelPrefRepository.save(userId, next);
        int updated = (request.email() == null ? 0 : 1) + (request.inbox() == null ? 0 : 1);
        return new ReaderPreferenceDtos.UpdateResult(updated, view(next));
    }

    private ReaderPreferenceDtos.ChannelsView view(ChannelPreferences preferences) {
        return new ReaderPreferenceDtos.ChannelsView(preferences.email(), preferences.inbox());
    }
}
