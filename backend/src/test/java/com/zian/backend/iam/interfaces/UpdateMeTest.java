package com.zian.backend.iam.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * M5-E：读者自助修改资料 PUT /v1/me。
 */
class UpdateMeTest extends SecurityTestSupport {

    @Test
    void updateProfileReturnsSameShapeAsMe() throws Exception {
        String userNo = newAccount("um");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        String body = objectMapper.writeValueAsString(
                Map.of("name", "新名字", "phone", "13800000001", "email", "new@example.com"));
        MvcResult updated = mockMvc.perform(put("/v1/me")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(updated.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, node.path("code").asInt(), node.toString());
        assertEquals("新名字", node.path("data").path("name").asText());
        assertEquals("READER", node.path("data").path("role").asText());

        MvcResult me = mockMvc.perform(get("/v1/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode meNode = objectMapper.readTree(me.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(meNode.path("data"), node.path("data"), "PUT /me 与 GET /me 结构应一致");

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT name, phone, email FROM sys_user WHERE id = ?", readerId);
        assertEquals("新名字", row.get("name"));
        assertEquals("13800000001", row.get("phone"));
        assertEquals("new@example.com", row.get("email"));

        // 传空串清空手机号与邮箱，未传的姓名字段保持不变。
        String clear = objectMapper.writeValueAsString(Map.of("phone", "", "email", ""));
        mockMvc.perform(put("/v1/me")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(clear))
                .andExpect(status().isOk());
        Map<String, Object> cleared = jdbc.queryForMap(
                "SELECT name, phone, email FROM sys_user WHERE id = ?", readerId);
        assertEquals("新名字", cleared.get("name"));
        assertNull(cleared.get("phone"));
        assertNull(cleared.get("email"));
    }

    @Test
    void invalidPhoneRejected() throws Exception {
        String userNo = newAccount("um");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);
        String body = objectMapper.writeValueAsString(Map.of("phone", "12345"));
        mockMvc.perform(put("/v1/me")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
