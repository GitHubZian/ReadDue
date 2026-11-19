package com.zian.backend.iam.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/** B-11 刷新令牌的接口级回归。 */
class AuthRefreshTest extends SecurityTestSupport {

    @Test
    @DisplayName("刷新返回新令牌，旧令牌立即失效、新令牌可继续访问")
    void refreshRotatesJtiAndInvalidatesOldToken() throws Exception {
        String userNo = newAccount("RFS");
        createUser(userNo, "READER", "NORMAL", null);
        String oldToken = login(userNo);

        String body = mockMvc.perform(post("/v1/auth/refresh").header(HttpHeaders.AUTHORIZATION, bearer(oldToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresIn").value("7200"))
                .andExpect(jsonPath("$.data.user.studentNo").value(userNo))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String newToken = objectMapper.readTree(body).path("data").path("token").asText();
        assertThat(newToken).isNotEqualTo(oldToken);

        mockMvc.perform(get("/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(oldToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(get("/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(newToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentNo").value(userNo));

        Integer audits = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action = 'AUTH_REFRESH' AND object_id = ?",
                Integer.class,
                jdbc.queryForObject(
                        "SELECT id FROM sys_user WHERE user_no = ?", Long.class, userNo));
        assertThat(audits).isEqualTo(1);
    }

    @Test
    @DisplayName("同一旧令牌只能刷新一次，二次刷新返回 401")
    void refreshTokenCanOnlyBeUsedOnce() throws Exception {
        String userNo = newAccount("RFS");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(post("/v1/auth/refresh").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/v1/auth/refresh").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("缺少或伪造令牌一律 401")
    void missingOrForgedTokenIsRejected() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/auth/refresh").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }
}
