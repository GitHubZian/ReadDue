package com.zian.backend.iam.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 登录失败限流：同一 IP/账号 1 分钟内第 6 次失败即 429；成功登录清零。
 */
class LoginRateLimitTest extends SecurityTestSupport {

    private static final String IP = "203.0.113.77";

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String userNo;

    @AfterEach
    void cleanCounters() {
        redisTemplate.delete(List.of(
                "readdue:auth:fail:ip:" + IP,
                "readdue:auth:fail:user:" + userNo,
                "readdue:auth:lock:ip:" + IP,
                "readdue:auth:lock:user:" + userNo));
    }

    @Test
    void sixthFailureIsThrottledAndSuccessResets() throws Exception {
        userNo = newAccount("rl");
        createUser(userNo, "READER", "NORMAL", null);
        String wrong = objectMapper.writeValueAsString(Map.of("userNo", userNo, "password", "bad-password"));

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/v1/auth/login")
                            .header("X-Forwarded-For", IP)
                            .contentType(JSON)
                            .content(wrong))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/v1/auth/login")
                        .header("X-Forwarded-For", IP)
                        .contentType(JSON)
                        .content(wrong))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(42900))
                .andExpect(jsonPath("$.message").value("尝试过于频繁，请稍后再试"));

        String right = objectMapper.writeValueAsString(Map.of("userNo", userNo, "password", DEMO_PASSWORD));
        mockMvc.perform(post("/v1/auth/login")
                        .header("X-Forwarded-For", IP)
                        .contentType(JSON)
                        .content(right))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 成功已清零：下一次失败回到普通 401，而不是继续 429。
        mockMvc.perform(post("/v1/auth/login")
                        .header("X-Forwarded-For", IP)
                        .contentType(JSON)
                        .content(wrong))
                .andExpect(status().isUnauthorized());
    }
}
