package com.zian.backend.grace.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MvcResult;

/**
 * M5-A：宽限申请的「场景授权 / 缓存授权」闸门。
 *
 * <p>场景授权关闭时按场景申请必须被拒；缓存授权关闭时不写 Redis 快照，重新打开后再关闭要清理旧快照。
 */
class GraceConsentGateTest extends SecurityTestSupport {

    private static final String SNAPSHOT_PREFIX = "readdue:grace:snap:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void sceneConsentOffBlocksSceneApply() throws Exception {
        String userNo = newAccount("gc");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        insertProfile(readerId, 0, 1);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        String token = login(userNo);

        String body = objectMapper.writeValueAsString(Map.of("borrowOrderId", orderId, "sceneCode", "EXAM"));
        MvcResult result = mockMvc.perform(post("/v1/graces")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().is(422))
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(42200, node.path("code").asInt());
        assertTrue(node.path("message").asText().contains("场景授权"));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM grace_application WHERE reader_id = ?", Integer.class, readerId));
    }

    @Test
    void cacheConsentOffSkipsSnapshot() throws Exception {
        String userNo = newAccount("gc");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        insertProfile(readerId, 1, 0);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        String token = login(userNo);

        apply(token, orderId, "NORMAL");
        assertFalse(redisTemplate.hasKey(SNAPSHOT_PREFIX + orderId));
    }

    @Test
    void revokingCacheConsentDeletesSnapshots() throws Exception {
        String userNo = newAccount("gc");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        insertProfile(readerId, 1, 1);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        String token = login(userNo);

        apply(token, orderId, "NORMAL");
        assertEquals(Boolean.TRUE, redisTemplate.hasKey(SNAPSHOT_PREFIX + orderId));

        String body = objectMapper.writeValueAsString(Map.of("cacheConsent", false));
        mockMvc.perform(put("/v1/me/consents")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().isOk());
        assertFalse(redisTemplate.hasKey(SNAPSHOT_PREFIX + orderId));
    }

    private void apply(String token, long orderId, String scene) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("borrowOrderId", orderId, "sceneCode", scene));
        MvcResult result = mockMvc.perform(post("/v1/graces")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, node.path("code").asInt(), node.toString());
    }

    private void insertProfile(long userId, int sceneConsent, int cacheConsent) {
        jdbc.update(
                """
                INSERT INTO reader_profile (user_id, scene_consent, cache_consent, created_at, updated_at)
                VALUES (?, ?, ?, NOW(), NOW())
                """,
                userId,
                sceneConsent,
                cacheConsent);
    }
}
