package com.zian.backend.notify.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 读者消息中心接口测试：只读本人、未读数、标记已读的范围与参数校验。
 */
class ReaderNotifyControllerTest extends SecurityTestSupport {

    @Test
    @DisplayName("列表只返回当前读者的站内信，未读标记正确")
    void listOnlyReturnsOwnMessages() throws Exception {
        long readerId = createUser(newAccount("N"), "READER", "NORMAL", null);
        long otherId = createUser(newAccount("N"), "READER", "NORMAL", null);
        inbox(readerId, "L2");
        inbox(otherId, "L3");
        String token = login(readerNo(readerId));

        MvcResult result = mockMvc.perform(get("/v1/notifications").header("Authorization", bearer(token)))
                .andReturn();
        JsonNode data = body(result).path("data");

        assertThat(data.path("total").asLong()).isEqualTo(1);
        JsonNode item = data.path("items").get(0);
        assertThat(item.path("level").asText()).isEqualTo("L2");
        assertThat(item.path("title").asText()).isNotBlank();
        assertThat(item.path("body").asText()).isNotBlank();
        assertThat(item.path("read").asBoolean()).isFalse();
        assertThat(data.path("items").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("未读数随标记已读递减，重复标记不再计数")
    void markReadUpdatesUnreadCount() throws Exception {
        long readerId = createUser(newAccount("N"), "READER", "NORMAL", null);
        long first = inbox(readerId, "L1");
        inbox(readerId, "L2");
        String token = login(readerNo(readerId));

        assertThat(unread(token)).isEqualTo(2);

        JsonNode updated = body(mockMvc.perform(put("/v1/notifications/read")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("ids", List.of(String.valueOf(first))))))
                .andReturn())
                .path("data");
        assertThat(updated.path("updated").asInt()).isEqualTo(1);
        assertThat(unread(token)).isEqualTo(1);

        JsonNode again = body(mockMvc.perform(put("/v1/notifications/read")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("ids", List.of(String.valueOf(first))))))
                .andReturn())
                .path("data");
        assertThat(again.path("updated").asInt()).isZero();
    }

    @Test
    @DisplayName("all=true 标记全部已读")
    void markAllRead() throws Exception {
        long readerId = createUser(newAccount("N"), "READER", "NORMAL", null);
        inbox(readerId, "L1");
        inbox(readerId, "L3");
        String token = login(readerNo(readerId));

        JsonNode updated = body(mockMvc.perform(put("/v1/notifications/read")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content("{\"all\":true}"))
                .andReturn())
                .path("data");

        assertThat(updated.path("updated").asInt()).isEqualTo(2);
        assertThat(unread(token)).isZero();
    }

    @Test
    @DisplayName("既没有 ids 也没有 all 时按参数错误拒绝")
    void markReadRequiresTargets() throws Exception {
        long readerId = createUser(newAccount("N"), "READER", "NORMAL", null);
        inbox(readerId, "L2");
        String token = login(readerNo(readerId));

        MvcResult result = mockMvc.perform(put("/v1/notifications/read")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content("{}"))
                .andReturn();

        assertThat(body(result).path("code").asInt()).isEqualTo(40000);
    }

    private int unread(String token) throws Exception {
        JsonNode data = body(mockMvc.perform(get("/v1/notifications/unread-count")
                        .header("Authorization", bearer(token)))
                .andReturn())
                .path("data");
        return data.path("unread").asInt();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String readerNo(long userId) {
        return jdbc.queryForObject("SELECT user_no FROM sys_user WHERE id = ?", String.class, userId);
    }

    private long inbox(long readerId, String level) {
        long taskId = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        long deliveryId = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        jdbc.update(
                """
                INSERT INTO notify_task (id, dedup_key, reader_id, level, plan_at, status, payload)
                VALUES (?, ?, ?, ?, NOW(), 'SENT', JSON_OBJECT('orderId', 1))
                """,
                taskId,
                "TEST:" + taskId,
                readerId,
                level);
        jdbc.update(
                """
                INSERT INTO notify_delivery (id, task_id, channel, status, attempt, sent_at)
                VALUES (?, ?, 'INBOX', 'SENT', 1, NOW())
                """,
                deliveryId,
                taskId);
        return deliveryId;
    }
}
