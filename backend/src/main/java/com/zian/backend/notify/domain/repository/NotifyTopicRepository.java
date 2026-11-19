package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.NotifyTopic;
import java.util.List;
import java.util.Optional;

/**
 * 通知主题字典。
 */
public interface NotifyTopicRepository {

    /**
     * 全部主题，按 sort 升序。
     *
     * @return 主题列表
     */
    List<NotifyTopic> listAll();

    /**
     * 按编码查找主题。
     *
     * @param code 主题编码
     * @return 不存在时为空
     */
    Optional<NotifyTopic> findByCode(String code);

    /**
     * 更新组织默认频率/渠道与启用状态。
     *
     * @param topicId 主题 ID
     * @param frequency 新默认频率
     * @param channels 新默认渠道
     * @param enabled 是否上线
     */
    void updateDefaults(long topicId, String frequency, List<String> channels, boolean enabled);
}
