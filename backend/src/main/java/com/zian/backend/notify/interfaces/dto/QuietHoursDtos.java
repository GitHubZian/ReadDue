package com.zian.backend.notify.interfaces.dto;

/**
 * 免打扰接口的请求与响应体。
 */
public final class QuietHoursDtos {

    private QuietHoursDtos() {}

    /**
     * 当前免打扰时段。
     *
     * @param enabled 是否启用
     * @param start   开始，`HH:mm`
     * @param end     结束，`HH:mm`；早于开始表示跨零点
     * @param source  `CUSTOM` 读者自定义 / `FALLBACK` 全局兜底
     */
    public record QuietHoursView(boolean enabled, String start, String end, String source) {}

    /**
     * 局部更新请求，字段为空表示保持不变。
     *
     * @param enabled 是否启用
     * @param start   开始，`HH:mm`
     * @param end     结束，`HH:mm`
     */
    public record UpdateRequest(Boolean enabled, String start, String end) {}
}
