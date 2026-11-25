package com.zian.backend.grace.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 快照过期后不允许审批（默认开关关闭），返回 409/40900。
 */
class GraceSnapshotExpiryTest extends SecurityTestSupport {

    @Test
    void expiredSnapshotCannotBeApproved() throws Exception {
        String userNo = newAccount("gx");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        jdbc.update(
                "INSERT INTO reader_profile (user_id, scene_consent, cache_consent, created_at, updated_at) "
                        + "VALUES (?, 1, 1, NOW(), NOW())",
                readerId);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        String reader = login(userNo);

        String applyBody = objectMapper.writeValueAsString(Map.of(
                "borrowOrderId", orderId,
                "sceneCode", "EXAM",
                "evidenceText", "快照过期测试材料"));
        String applied = mockMvc.perform(post("/v1/graces")
                        .header("Authorization", bearer(reader))
                        .contentType(JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(applied);
        long graceId = node.path("data").path("id").asLong();
        jdbc.update(
                "UPDATE grace_application SET snapshot_expires_at = DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE id = ?",
                graceId);

        String admin = login("ADMIN001");
        mockMvc.perform(post("/v1/admin/graces/{id}/approve", graceId)
                        .header("Authorization", bearer(admin))
                        .contentType(JSON)
                        .content("{\"days\":2,\"note\":\"过期后审批\"}".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900))
                .andExpect(jsonPath("$.message").value("申请快照已过期，请读者重新提交"));
    }
}
