package com.zian.backend.shared.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 对外时间格式：统一 {@code +08:00} 的 ISO-8601 字符串。
 *
 * <p>全局 Jackson 以 UTC 输出 {@code OffsetDateTime}，会把管理端时间变成 {@code Z} 结尾；读者端接口直接把
 * 时间格式化为字符串，保证两端一致。
 */
public final class Times {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private Times() {
    }

    /**
     * 把本地时间格式化为东八区 ISO-8601 字符串。
     *
     * @param time 本地时间，可为 null
     * @return 带 +08:00 的字符串；入参为 null 时返回 null
     */
    public static String iso(LocalDateTime time) {
        return time == null ? null : time.atZone(ZONE).toOffsetDateTime().toString();
    }
}
