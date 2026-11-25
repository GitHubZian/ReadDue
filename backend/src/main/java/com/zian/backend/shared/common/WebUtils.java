package com.zian.backend.shared.common;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Web 请求信息工具
 */
public class WebUtils {

    private static final String UNKNOWN = "unknown";

    private WebUtils() {
    }

    /**
     * 取客户端 IP。
     *
     * <p>只有直连方是本机反向代理时才采信 {@code X-Forwarded-For} / {@code X-Real-IP}；
     * 其它情况一律用 {@code remoteAddr}，避免客户端自带转发头伪造审计来源。
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String remoteAddr = request.getRemoteAddr();
        // 只信任本机/内网反向代理（docker 网关常是 172.x），公网直连时转发头一律忽略。
        boolean trustedProxy = isPrivateOrLoopback(remoteAddr);
        String ip = null;
        if (trustedProxy) {
            ip = request.getHeader("X-Forwarded-For");
            if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
        }
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = remoteAddr;
        }
        // X-Forwarded-For 可能是「客户端, 代理1, 代理2」，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip) ? null : ip;
    }

    /** 回环或 RFC1918 私网地址。 */
    private static boolean isPrivateOrLoopback(String address) {
        if (address == null) {
            return false;
        }
        if ("127.0.0.1".equals(address) || "0:0:0:0:0:0:0:1".equals(address) || "::1".equals(address)) {
            return true;
        }
        if (address.startsWith("10.") || address.startsWith("192.168.") || address.startsWith("127.")) {
            return true;
        }
        if (address.startsWith("172.")) {
            String[] parts = address.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return address.startsWith("fc") || address.startsWith("fd") || address.startsWith("fe80:");
    }

    /**
     * 取 User-Agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request == null ? null : request.getHeader("User-Agent");
    }
}
