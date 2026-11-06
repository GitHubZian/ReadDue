package com.zian.backend.iam.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 认证与账号相关 DTO。
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    /** 登录请求：userNo 为学号/工号；前端也使用 username 字段，二者等价 */
    public record LoginRequest(
            @com.fasterxml.jackson.annotation.JsonAlias({"username", "user_no"})
                    @NotBlank(message = "学号/工号不能为空")
                    String userNo,
            @NotBlank(message = "密码不能为空") String password) {
    }

    /** 当前会话用户（前端 SessionUser） */
    public record SessionUser(
            Long id, String name, String role, String studentNo, String avatarUrl, List<String> permissions) {
    }

    /** 登录结果（前端 LoginResult） */
    public record LoginResult(String token, long expiresIn, SessionUser user) {
    }

    /** 拉黑请求 */
    public record BlacklistRequest(@NotBlank(message = "拉黑必须填写原因") String reason, String expiresAt) {
    }

    /** 账号状态变更结果 */
    public record UserStatusView(Long id, String status, String blacklistReason) {
    }
}
