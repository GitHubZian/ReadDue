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
}
