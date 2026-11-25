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

    /** 当前会话用户（前端 SessionUser）；department/major/grade 无档案时为空字符串，totalBorrowed 含已归还。
     * phone/email 仅返回给本人，用于资料编辑回填与提醒渠道。 */
    public record SessionUser(
            Long id,
            String name,
            String role,
            String studentNo,
            String avatarUrl,
            List<String> permissions,
            String department,
            String major,
            String grade,
            long totalBorrowed,
            String phone,
            String email) {
    }

    /** 登录结果（前端 LoginResult） */
    public record LoginResult(String token, long expiresIn, SessionUser user) {
    }

    /**
     * 读者自助修改资料：只允许姓名/手机/邮箱。
     *
     * <p>字段为 null 表示不修改；手机/邮箱传空串表示清空。
     */
    public record UpdateProfileRequest(
            @jakarta.validation.constraints.Size(min = 1, max = 20, message = "姓名长度为 1-20 个字符")
                    String name,
            @jakarta.validation.constraints.Pattern(
                            regexp = "^$|^1\\d{10}$", message = "手机号需为 11 位数字且以 1 开头")
                    String phone,
            @jakarta.validation.constraints.Email(message = "邮箱格式不正确")
                    @jakarta.validation.constraints.Size(max = 100, message = "邮箱过长")
                    String email) {
    }

    /** 拉黑请求 */
    public record BlacklistRequest(@NotBlank(message = "拉黑必须填写原因") String reason, String expiresAt) {
    }

    /** 账号状态变更结果 */
    public record UserStatusView(Long id, String status, String blacklistReason) {
    }
}
