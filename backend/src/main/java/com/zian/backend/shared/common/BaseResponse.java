package com.zian.backend.shared.common;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 通用返回类。
 *
 * <p>响应体固定四段：{@code code}（0 表示成功）、{@code message}、{@code data}、{@code traceId}
 * （与请求头 {@code X-Trace-Id} 一致，前端报障时可直接提供）。
 *
 * @param <T> 业务数据类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;

    private T data;

    private String message;

    /** 链路追踪 id，取自 MDC（TraceIdFilter 写入） */
    private String traceId;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.traceId = TraceContext.current();
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
