package com.zian.backend.iam.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * M6-②：注销申请流程——读者提交、管理员驳回/批准，批准复用立即遗忘脱敏。
 */
class DeletionRequestTest extends SecurityTestSupport {

    @Test
    void readerRequestThenAdminRejectAndApprove() throws Exception {
        String userNoA = newAccount("del");
        long readerA = createUser(userNoA, "READER", "NORMAL", null);
        String tokenA = bearer(login(userNoA));

        JsonNode created = postJson("/v1/me/deletion-request", tokenA, "{\"reason\":\"毕业离校\"}");
        assertThat(created.path("code").asInt()).isZero();
        String idA = created.path("data").path("id").asText();
        assertThat(created.path("data").path("status").asText()).isEqualTo("PENDING");

        // 同一读者重复提交 → 409
        mockMvc.perform(post("/v1/me/deletion-request")
                        .contentType(JSON)
                        .content("{}")
                        .header("Authorization", tokenA))
                .andExpect(status().isConflict());

        JsonNode mine = getJson("/v1/me/deletion-request", tokenA);
        assertThat(mine.path("data").path("id").asText()).isEqualTo(idA);

        String admin = bearer(login("ADMIN001"));
        JsonNode pending = getJson("/v1/admin/deletion-requests?status=PENDING", admin);
        assertThat(pending.path("data").findValuesAsText("id")).contains(idA);

        JsonNode rejected = postJson(
                "/v1/admin/deletion-requests/" + idA + "/reject", admin, "{\"note\":\"请到馆办理\"}");
        assertThat(rejected.path("data").path("status").asText()).isEqualTo("REJECTED");
        assertThat(rejected.path("data").path("note").asText()).isEqualTo("请到馆办理");

        // 读者 B：批准后姓名脱敏、手机号清空，申请单标记 APPROVED。
        String userNoB = newAccount("del");
        long readerB = createUser(userNoB, "READER", "NORMAL", null);
        jdbc.update("UPDATE sys_user SET phone = '13800000009' WHERE id = ?", readerB);
        String tokenB = bearer(login(userNoB));
        JsonNode createdB = postJson("/v1/me/deletion-request", tokenB, "{\"reason\":\"隐私考虑\"}");
        String idB = createdB.path("data").path("id").asText();

        JsonNode approved = postJson("/v1/admin/deletion-requests/" + idB + "/approve", admin, "{}");
        assertThat(approved.path("data").path("status").asText()).isEqualTo("APPROVED");
        assertThat(approved.path("data").path("handledBy").asText()).isNotBlank();

        String name = jdbc.queryForObject("SELECT name FROM sys_user WHERE id = ?", String.class, readerB);
        String phone = jdbc.queryForObject("SELECT phone FROM sys_user WHERE id = ?", String.class, readerB);
        assertThat(name).startsWith("已注销读者-");
        assertThat(phone).isNull();

        assertThat(countAudit("DELETION_REQUEST_CREATE", idA)).isEqualTo(1);
        assertThat(countAudit("DELETION_REQUEST_REJECT", idA)).isEqualTo(1);
        assertThat(countAudit("DELETION_REQUEST_APPROVE", idB)).isEqualTo(1);

        // 已处理的申请再次批准 → 409
        mockMvc.perform(post("/v1/admin/deletion-requests/" + idB + "/approve")
                        .header("Authorization", admin))
                .andExpect(status().isConflict());
    }

    private int countAudit(String action, String objectId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action = ? AND object_id = ?",
                Integer.class,
                action,
                Long.parseLong(objectId));
        return count == null ? 0 : count;
    }

    private JsonNode getJson(String path, String bearer) throws Exception {
        String body = mockMvc.perform(get(path).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private JsonNode postJson(String path, String bearer, String json) throws Exception {
        String body = mockMvc.perform(post(path)
                        .contentType(JSON)
                        .content(json)
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }
}
