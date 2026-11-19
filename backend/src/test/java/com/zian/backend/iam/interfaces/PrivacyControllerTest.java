package com.zian.backend.iam.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/** B-10 隐私授权与遗忘的接口级回归。 */
class PrivacyControllerTest extends SecurityTestSupport {

    @Test
    @DisplayName("新读者默认最小授权，且看不到任何已授权项")
    void defaultConsentsAreMinimal() throws Exception {
        String userNo = newAccount("PRV");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(get("/v1/me/consents").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sceneConsent").value(false))
                .andExpect(jsonPath("$.data.cacheConsent").value(false))
                .andExpect(jsonPath("$.data.consentRevokedAt").doesNotExist())
                .andExpect(jsonPath("$.data.forgotten").value(false));
    }

    @Test
    @DisplayName("关闭任一授权会记录关闭时间并写 CONSENT_REVOKE 审计")
    void revokingConsentStampsTimeAndAudit() throws Exception {
        String userNo = newAccount("PRV");
        long userId = createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(put("/v1/me/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content("{\"sceneConsent\":true,\"cacheConsent\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sceneConsent").value(true))
                .andExpect(jsonPath("$.data.cacheConsent").value(true));

        String body = mockMvc.perform(put("/v1/me/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content("{\"cacheConsent\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cacheConsent").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(body).path("data").path("consentRevokedAt").asText())
                .isNotEmpty();

        Integer revoked = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action = 'CONSENT_REVOKE' AND object_id = ?",
                Integer.class,
                userId);
        assertThat(revoked).isEqualTo(1);

        mockMvc.perform(put("/v1/me/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("立即遗忘会脱敏姓名/手机/邮箱/院系并保留借阅事实，重复提交返回 409")
    void forgetAnonymizesAndKeepsFacts() throws Exception {
        String userNo = newAccount("PRV");
        long userId = createUser(userNo, "READER", "NORMAL", null);
        jdbc.update(
                "UPDATE sys_user SET phone = '13800001111', email = 'pii@example.com' WHERE id = ?", userId);
        jdbc.update(
                "INSERT INTO reader_profile (user_id, department, major, grade, scene_consent, cache_consent, created_at, updated_at) "
                        + "VALUES (?, '计算机学院', '软件工程', '2023', 1, 1, NOW(), NOW())",
                userId);
        long loanId = createActiveLoan(userId, createBookWithCopy("ON_LOAN"));
        String token = login(userNo);

        String body = mockMvc.perform(post("/v1/me/forget").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.forgotten").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode data = objectMapper.readTree(body).path("data");
        assertThat(data.path("displayName").asText()).startsWith("已注销读者-");

        Map<String, Object> user = jdbc.queryForMap(
                "SELECT name, phone, email FROM sys_user WHERE id = ?", userId);
        assertThat(user.get("phone")).isNull();
        assertThat(user.get("email")).isNull();
        assertThat((String) user.get("name")).startsWith("已注销读者-");

        Map<String, Object> profile = jdbc.queryForMap(
                "SELECT department, major, grade, scene_consent, cache_consent, forget_requested_at "
                        + "FROM reader_profile WHERE user_id = ?",
                userId);
        assertThat(profile.get("department")).isNull();
        assertThat(profile.get("major")).isNull();
        assertThat(profile.get("scene_consent")).isEqualTo(0);
        assertThat(profile.get("forget_requested_at")).isNotNull();

        Integer loans = jdbc.queryForObject(
                "SELECT COUNT(*) FROM borrow_order WHERE id = ? AND reader_id = ?", Integer.class, loanId, userId);
        assertThat(loans).isEqualTo(1);

        Integer audits = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action = 'USER_FORGET' AND object_id = ?",
                Integer.class,
                userId);
        assertThat(audits).isEqualTo(1);
        List<String> actions = jdbc.queryForList(
                "SELECT action FROM audit_log WHERE object_id = ? ORDER BY id DESC LIMIT 1", String.class, userId);
        assertThat(actions).containsExactly("USER_FORGET");

        mockMvc.perform(post("/v1/me/forget").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    @DisplayName("匿名访问隐私接口一律 401")
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/v1/me/consents")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/me/forget")).andExpect(status().isUnauthorized());
    }
}
