package com.zian.backend.iam.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

/**
 * M5-B：遗忘级联——删除宽限快照键与该读者的宽限证据文件（DB 软删 + 物理删除）。
 */
class PrivacyForgetCascadeTest extends SecurityTestSupport {

    private static final String SNAPSHOT_PREFIX = "readdue:grace:snap:";
    private static final String REAL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${app.file.storage-dir}")
    private String storageDir;

    @Test
    void forgetRemovesSnapshotsAndEvidenceFiles() throws Exception {
        String userNo = newAccount("pf");
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

        long fileId = uploadEvidence(token);
        String applyBody = objectMapper.writeValueAsString(
                Map.of("borrowOrderId", orderId, "sceneCode", "NORMAL", "evidenceIds", java.util.List.of(fileId)));
        MvcResult applied = mockMvc.perform(post("/v1/graces")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode applyNode =
                objectMapper.readTree(applied.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, applyNode.path("code").asInt(), applyNode.toString());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM grace_evidence WHERE file_id = ?", Integer.class, fileId));

        redisTemplate.opsForValue().set(SNAPSHOT_PREFIX + orderId, "{}");
        String storagePath = jdbc.queryForObject(
                "SELECT storage_path FROM file_object WHERE id = ?", String.class, fileId);
        Path physical = Path.of(storageDir).resolve(storagePath);
        assertTrue(Files.exists(physical), "证据文件应已落盘");

        MvcResult forgotten = mockMvc.perform(
                        post("/v1/me/forget").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node =
                objectMapper.readTree(forgotten.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, node.path("code").asInt(), node.toString());
        assertTrue(node.path("data").path("forgotten").asBoolean());

        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT deleted FROM file_object WHERE id = ?", Integer.class, fileId));
        assertFalse(Files.exists(physical), "证据文件物理文件应已删除");
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM grace_evidence WHERE file_id = ?", Integer.class, fileId));
        assertFalse(redisTemplate.hasKey(SNAPSHOT_PREFIX + orderId));

        String detail = jdbc.queryForObject(
                "SELECT detail FROM audit_log WHERE action = 'USER_FORGET' AND object_id = ?",
                String.class,
                readerId);
        assertTrue(detail.contains("removedSnapshots"), detail);
        assertTrue(detail.contains("removedFiles"), detail);
    }

    private long uploadEvidence(String token) throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "evidence.png", "image/png", Base64.getDecoder().decode(REAL_PNG));
        MvcResult result = mockMvc.perform(multipart("/v1/files").file(file).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, node.path("code").asInt(), node.toString());
        return node.path("data").path("id").asLong();
    }
}
