package com.zian.backend.grace.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * M5-D：宽限建议预览——只计算不落库，且校验订单归属。
 */
class GracePreviewTest extends SecurityTestSupport {

    @Test
    void previewComputesWithoutPersisting() throws Exception {
        String userNo = newAccount("gv");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        jdbc.update(
                """
                INSERT INTO reader_profile (user_id, scene_consent, cache_consent, created_at, updated_at)
                VALUES (?, 1, 1, NOW(), NOW())
                """,
                readerId);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        String token = login(userNo);

        String body = objectMapper.writeValueAsString(
                Map.of("orderId", orderId, "sceneCode", "EXAM", "requestedDays", 2));
        MvcResult result = mockMvc.perform(post("/v1/graces/preview")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, node.path("code").asInt(), node.toString());
        JsonNode data = node.path("data");
        assertTrue(data.path("baseDays").asInt() >= 1, data.toString());
        assertTrue(data.path("capDays").asInt() >= 1, data.toString());
        assertTrue(data.path("sceneFactor").asDouble() > 0, data.toString());
        assertTrue(data.path("quotaFactor").asDouble() > 0, data.toString());
        assertTrue(data.path("suggestedDays").asDouble() > 0, data.toString());
        assertTrue(data.path("note").asText().length() > 0, data.toString());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM grace_application WHERE reader_id = ?", Integer.class, readerId));
        assertEquals(0, jdbc.queryForObject(
                        "SELECT COUNT(*) FROM audit_log WHERE action = 'GRACE_APPLY' AND object_id = ?",
                        Integer.class,
                        orderId));
    }

    @Test
    void previewRejectsOtherReadersOrder() throws Exception {
        long ownerId = createUser(newAccount("gv"), "READER", "NORMAL", null);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(ownerId, fixture);

        String otherNo = newAccount("gv");
        createUser(otherNo, "READER", "NORMAL", null);
        String token = login(otherNo);
        String body = objectMapper.writeValueAsString(Map.of("orderId", orderId, "sceneCode", "NORMAL"));
        mockMvc.perform(post("/v1/graces/preview")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().is(403));
    }
}
