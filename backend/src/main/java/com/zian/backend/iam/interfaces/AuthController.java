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

    @PostMapping("/auth/login")
    public BaseResponse<AuthDtos.LoginResult> login(@RequestBody @Valid AuthDtos.LoginRequest request) {
        return ResultUtils.success(authService.login(request));
    }

    @GetMapping("/me")
    public BaseResponse<AuthDtos.SessionUser> me() {
        return ResultUtils.success(authService.me());
    }

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
