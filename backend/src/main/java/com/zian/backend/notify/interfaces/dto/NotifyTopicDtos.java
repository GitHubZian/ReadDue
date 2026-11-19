package com.zian.backend.notify.interfaces.dto;

import java.util.List;

/**
 * 管理端通知主题接口的请求与响应体。
 */
public final class NotifyTopicDtos {

    private NotifyTopicDtos() {}

    /**
     * 主题字典项。
     *
     * @param topicCode        主题编码
     * @param name             展示名
     * @param priority         优先级 P0/P1/P2
     * @param defaultFrequency 组织默认频率
     * @param defaultChannels  组织默认渠道
     * @param enabled          是否上线
     * @param sort             排序值
     */
    public record AdminTopicView(
            String topicCode,
            String name,
            String priority,
            String defaultFrequency,
            List<String> defaultChannels,
            boolean enabled,
            int sort) {}

    /**
     * 更新组织默认值；字段为空表示保持不变。
     *
     * @param defaultFrequency 默认频率
     * @param defaultChannels  默认渠道
     * @param enabled          是否上线
     */
    public record UpdateDefaultsRequest(
            String defaultFrequency, List<String> defaultChannels, Boolean enabled) {}
}
