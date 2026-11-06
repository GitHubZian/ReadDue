package com.zian.backend.shared.security;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;

/**
 * 请求级登录主体持有器（ThreadLocal）。
 *
 * <p>由 {@code JwtAuthenticationFilter} 在请求开始时写入、请求结束时清理；
 * 应用层通过 {@link #current()} / {@link #require()} 获取当前操作者，避免层层透传。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser current() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    public static Long requireId() {
        return require().id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
