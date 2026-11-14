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

    /**
     * 绑定当前请求的登录用户。
     *
     * @param user 登录用户
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 读取当前请求的登录用户。
     *
     * @return 登录用户，未登录时返回 null
     */
    public static LoginUser current() {
        return HOLDER.get();
    }

    /**
     * 读取当前请求的登录用户，未登录直接抛 401。
     *
     * @return 登录用户
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 读取当前登录用户 ID，未登录直接抛 401。
     *
     * @return 用户 ID
     */
    public static Long requireId() {
        return require().id();
    }

    /**
     * 清理当前线程的登录用户，避免线程复用串号。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
