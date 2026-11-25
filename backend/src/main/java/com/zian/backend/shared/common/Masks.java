package com.zian.backend.shared.common;

/**
 * 日志脱敏工具：个人信息只保留可识别前缀，避免完整写进日志。
 */
public final class Masks {

    private Masks() {
    }

    /**
     * 邮箱脱敏：保留首字符与域名，例如 {@code 2927251542@qq.com} → {@code 2***@qq.com}。
     *
     * @param address 邮箱地址，可为 null
     * @return 脱敏后的字符串；空值或非法格式返回 {@code ***}
     */
    public static String email(String address) {
        if (address == null) {
            return "***";
        }
        String clean = sanitize(address).trim();
        if (clean.isEmpty()) {
            return "***";
        }
        int at = clean.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return clean.charAt(0) + "***" + clean.substring(at);
    }

    /**
     * 清洗自由文本：控制字符替换为空格并截断，防止伪造日志行与超出列宽。
     *
     * @param value     原始文本，可为 null
     * @param maxLength 允许的最大长度
     * @return 清洗后的文本；null 返回空串
     */
    public static String text(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String clean = sanitize(value);
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    private static String sanitize(String value) {
        return value.replaceAll("[\\r\\n\\t]", " ");
    }

    /**
     * 姓名脱敏：保留首字符，例如 {@code 张三} → {@code 张**}。
     *
     * @param name 姓名，可为 null
     * @return 脱敏后的姓名；空值返回 {@code ***}
     */
    public static String name(String name) {
        if (name == null) {
            return "***";
        }
        String clean = sanitize(name).trim();
        if (clean.isEmpty()) {
            return "***";
        }
        return clean.charAt(0) + "**";
    }

    /**
     * 手机号脱敏：保留前三位与后四位，例如 {@code 13812345678} → {@code 138****5678}。
     *
     * @param phone 手机号，可为 null
     * @return 脱敏后的手机号；空值或过短返回 {@code ***}
     */
    public static String phone(String phone) {
        if (phone == null) {
            return "***";
        }
        String clean = sanitize(phone).trim();
        if (clean.length() < 7) {
            return "***";
        }
        return clean.substring(0, 3) + "****" + clean.substring(clean.length() - 4);
    }

    /**
     * IP 脱敏：IPv4 保留前两段，IPv6 保留前两组。
     *
     * @param ip IP 地址，可为 null
     * @return 脱敏后的 IP；空值返回 {@code ***}
     */
    public static String ip(String ip) {
        if (ip == null) {
            return "***";
        }
        String clean = sanitize(ip).trim();
        if (clean.isEmpty()) {
            return "***";
        }
        if (clean.contains(":")) {
            String[] groups = clean.split(":");
            return groups.length >= 2 ? groups[0] + ":" + groups[1] + ":***" : "***";
        }
        String[] parts = clean.split("\\.");
        return parts.length >= 2 ? parts[0] + "." + parts[1] + ".*.*" : "***";
    }

    /**
     * JSON 文本脱敏：把 name/phone/mobile/email/ip 字段的值替换为 {@code ***}，再清洗与截断。
     *
     * @param raw       JSON 文本，可为 null
     * @param maxLength 最大长度
     * @return 脱敏后的文本；null 返回空串
     */
    public static String json(String raw, int maxLength) {
        if (raw == null) {
            return "";
        }
        String clean = sanitize(raw).replaceAll(
                "(?i)(\"(?:name|phone|mobile|email|ip)\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
        return text(clean, maxLength);
    }
}
