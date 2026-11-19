package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.ChannelPreferences;

/**
 * 读者渠道偏好。
 */
public interface ChannelPrefRepository {

    /**
     * 读取读者两个渠道的开关；没有记录的渠道视为开启。
     *
     * @param userId 读者用户 ID
     * @return 渠道偏好
     */
    ChannelPreferences load(long userId);

    /**
     * 保存读者渠道偏好（按渠道 upsert）。
     *
     * @param userId 读者用户 ID
     * @param preferences 目标偏好
     */
    void save(long userId, ChannelPreferences preferences);
}
