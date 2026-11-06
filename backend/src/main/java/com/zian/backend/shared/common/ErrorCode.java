package com.zian.backend.shared.common;

import lombok.Getter;

/**
 * 自定义错误码。
 *
 * <p>业务码前两位对应 HTTP 状态族（40x/42x/50x），由 {@link #statusOf(int)} 统一映射，
 * 保证「响应体 code」与「HTTP 状态码」语义一致：
 * 400 参数、401 未登录、403 越权、404 不存在、409 并发冲突、422 规则拒绝、429 限流、500 系统错误。
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录或登录已过期"),
    NO_AUTH_ERROR(40301, "无权限"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    CONFLICT_ERROR(40900, "数据已被修改，请刷新后重试"),
    RULE_VIOLATION(42200, "业务规则不满足"),
    TOO_MANY_REQUEST(42900, "请求过于频繁"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 业务码 → HTTP 状态码。
     */
    public static int statusOf(int code) {
        if (code == 0) {
            return 200;
        }
        return switch (code / 100) {
            case 400 -> 400;
            case 401 -> 401;
            case 403 -> 403;
            case 404 -> 404;
            case 409 -> 409;
            case 422 -> 422;
            case 429 -> 429;
            case 500 -> 500;
            default -> 400;
        };
    }

    public int getHttpStatus() {
        return statusOf(code);
    }

}
