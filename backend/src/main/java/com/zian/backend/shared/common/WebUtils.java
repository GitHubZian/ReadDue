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
        boolean trustedProxy = "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
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

    /**
     * 取 User-Agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request == null ? null : request.getHeader("User-Agent");
    }
}
