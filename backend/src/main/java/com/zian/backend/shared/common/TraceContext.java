package com.zian.backend.shared.common;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

/**
 * 链路追踪上下文。
 *
 * <p>traceId 由 {@code TraceIdFilter} 从请求头 {@code X-Trace-Id} 读取（缺省生成），
 * 写入 MDC 供日志输出，并随 {@code BaseResponse.traceId} 一起返回给前端用于报障定位。
 */
public final class TraceContext {

    public static final String HEADER = "X-Trace-Id";

    private static final String MDC_KEY = "traceId";

    private TraceContext() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static void set(String traceId) {
        MDC.put(MDC_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }

    /**
     * 归一化外部传入的 traceId：只接受 8~64 位可见字符，其余一律重新生成，避免日志注入。
     */
    public static String normalize(String incoming) {
        if (StrUtil.isBlank(incoming)) {
            return generate();
        }
        String v = incoming.trim();
        if (v.length() < 8 || v.length() > 64 || !v.matches("[A-Za-z0-9._:-]+")) {
            return generate();
        }
        return v;
    }

    public static String generate() {
        return IdUtil.fastSimpleUUID().substring(0, 16);
    }
}
