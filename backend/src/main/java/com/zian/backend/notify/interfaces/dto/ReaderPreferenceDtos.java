package com.zian.backend.notify.interfaces.dto;

/**
 * 读者渠道偏好接口的请求与响应体。
 */
public final class ReaderPreferenceDtos {

    private ReaderPreferenceDtos() {}

    /**
     * 当前渠道开关。
     *
     * @param email 邮件提醒是否开启
     * @param inbox 站内信是否开启
     */
    public record ChannelsView(boolean email, boolean inbox) {}

    /**
     * 局部更新请求：只改传入的字段。
     *
     * @param email 邮件开关，null 表示不变
     * @param inbox 站内信开关，null 表示不变
     */
    public record UpdateRequest(Boolean email, Boolean inbox) {}

    /**
     * 更新结果。
     *
     * @param updated 实际写入的渠道数（0–2）
     * @param channels 更新后的完整开关
     */
    public record UpdateResult(int updated, ChannelsView channels) {}
}
