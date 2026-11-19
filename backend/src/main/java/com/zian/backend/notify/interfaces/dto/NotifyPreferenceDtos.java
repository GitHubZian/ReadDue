package com.zian.backend.notify.interfaces.dto;

import java.util.List;

/**
 * FR-11 通知偏好接口的请求与响应体。
 */
public final class NotifyPreferenceDtos {

    private NotifyPreferenceDtos() {}

    /**
     * 主题 + 当前生效偏好。
     *
     * @param topicCode 主题编码
     * @param name      展示名
     * @param priority  优先级 P0/P1/P2
     * @param locked    P0 必达，站内信不可关闭
     * @param frequency 生效频率
     * @param channels  生效渠道
     * @param customized 是否读者自定义（false 表示沿用组织默认）
     */
    public record TopicView(
            String topicCode,
            String name,
            String priority,
            boolean locked,
            String frequency,
            List<String> channels,
            boolean customized) {}

    /**
     * 一条自定义偏好（只列与默认不同的主题）。
     *
     * @param topicCode 主题编码
     * @param frequency 频率
     * @param channels  渠道
     */
    public record PreferenceView(String topicCode, String frequency, List<String> channels) {}

    /**
     * 批量更新的单项。
     *
     * @param topicCode 主题编码
     * @param frequency 频率 REALTIME/DAILY/WEEKLY
     * @param channels  渠道 INBOX/EMAIL
     */
    public record UpdateItem(String topicCode, String frequency, List<String> channels) {}

    /**
     * 批量更新请求。
     *
     * @param items 待更新项，至少一条
     */
    public record UpdateRequest(List<UpdateItem> items) {}

    /**
     * 批量更新结果。
     *
     * @param updated     实际变更条数
     * @param preferences 更新后的自定义偏好
     */
    public record UpdateResult(int updated, List<PreferenceView> preferences) {}

    /**
     * 一条偏好变更日志。
     *
     * @param topicCode 主题编码
     * @param before    变更前偏好 JSON，首次设置为 null
     * @param after     变更后偏好 JSON，恢复默认为 null
     * @param createdAt 时间
     */
    public record LogView(String topicCode, String before, String after, String createdAt) {}
}
