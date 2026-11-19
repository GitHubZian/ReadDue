package com.zian.backend.notify.domain.model;

import java.util.List;

/**
 * 读者对某个通知主题的偏好，只在与组织默认不同时才存在。
 *
 * @param id        主键
 * @param userId    读者用户 ID
 * @param topicId   主题 ID
 * @param frequency 频率：REALTIME/DAILY/WEEKLY
 * @param channels  渠道集合
 */
public record NotifyPreference(
        Long id, long userId, long topicId, String frequency, List<String> channels) {
}
