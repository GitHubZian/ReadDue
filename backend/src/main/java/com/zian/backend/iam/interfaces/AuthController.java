package com.zian.backend.iam.interfaces;

import com.zian.backend.iam.application.AuthService;
import com.zian.backend.iam.interfaces.dto.AuthDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 账号密码登录：校验凭据、记录登录时间并签发 JWT。
     *
     * @param request 登录账号（学号/工号）与密码
     * @return token、有效期与用户信息
     */
    @PostMapping("/auth/login")
    public BaseResponse<AuthDtos.LoginResult> login(@RequestBody @Valid AuthDtos.LoginRequest request) {
        return ResultUtils.success(authService.login(request));
    }

    /**
     * 返回当前登录用户信息，角色与权限码以数据库为准。
     *
     * @return 会话用户
     */
    @GetMapping("/me")
    public BaseResponse<AuthDtos.SessionUser> me() {
        return ResultUtils.success(authService.me());
    }

    /**
     * 注销当前 token：删除 Redis 中的登录态，使该 token 立即失效。
     *
     * @param authorization Authorization 请求头（Bearer token）
     * @return 空响应
     */
    @PostMapping("/auth/logout")
    public BaseResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(resolveToken(authorization));
        return ResultUtils.success(null);
    }

    private String resolveToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length()).trim();
    }
}
