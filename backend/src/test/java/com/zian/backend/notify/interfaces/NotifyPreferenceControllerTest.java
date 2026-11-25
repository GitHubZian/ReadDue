package com.zian.backend.notify.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.notify.application.NotifyService;
import com.zian.backend.security.SecurityTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

/**
 * FR-11 通知偏好：主题默认值、差异存储、硬约束与排程快照。
 */
class NotifyPreferenceControllerTest extends SecurityTestSupport {

    @Autowired
    private NotifyService notifyService;

    @Test
    @DisplayName("默认返回六个主题，P0 标记为必达")
    void topicsExposeDefaults() throws Exception {
        String userNo = newAccount("np");
        createUser(userNo, "READER", "NORMAL", null);

        mockMvc.perform(get("/v1/me/notify-topics").header(HttpHeaders.AUTHORIZATION, bearer(login(userNo))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].topicCode").value("BORROW_DUE"))
                .andExpect(jsonPath("$.data[1].topicCode").value("BORROW_OVERDUE"))
                .andExpect(jsonPath("$.data[1].locked").value(true))
                .andExpect(jsonPath("$.data[1].channels.length()").value(2))
                .andExpect(jsonPath("$.data[4].priority").value("P1"))
                .andExpect(jsonPath("$.data[5].topicCode").value("NOTICE_GENERAL"))
                .andExpect(jsonPath("$.data[0].customized").value(false));
    }

    @Test
    @DisplayName("自定义偏好落差异行并写日志，恢复默认后差异清空")
    void updateStoresDiffAndLog() throws Exception {
        String userNo = newAccount("np");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "items",
                                List.of(Map.of(
                                        "topicCode", "BORROW_DUE",
                                        "frequency", "WEEKLY",
                                        "channels", List.of("EMAIL")))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updated").value(1))
                .andExpect(jsonPath("$.data.preferences.length()").value(1))
                .andExpect(jsonPath("$.data.preferences[0].topicCode").value("BORROW_DUE"))
                .andExpect(jsonPath("$.data.preferences[0].frequency").value("WEEKLY"));

        mockMvc.perform(get("/v1/me/notify-topics").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.data[0].customized").value(true))
                .andExpect(jsonPath("$.data[0].channels[0]").value("EMAIL"));

        mockMvc.perform(get("/v1/me/notify-preferences/log").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].topicCode").value("BORROW_DUE"))
                .andExpect(jsonPath("$.data[0].before").doesNotExist())
                .andExpect(jsonPath("$.data[0].after").isString());

        // 改回组织默认（DAILY + INBOX/EMAIL）→ 差异行删除，日志 after 为 null。
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "items",
                                List.of(Map.of(
                                        "topicCode", "BORROW_DUE",
                                        "frequency", "DAILY",
                                        "channels", List.of("INBOX", "EMAIL")))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferences.length()").value(0));

        mockMvc.perform(get("/v1/me/notify-preferences/log").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].after").doesNotExist());
    }

    @Test
    @DisplayName("P0 缺站内信、P2 实时、SMS 渠道、非法频率与未知主题分别被拒绝")
    void constraintMatrix() throws Exception {
        String userNo = newAccount("np");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        // P0 必达：不能只留 EMAIL
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(payload("BORROW_OVERDUE", "DAILY", List.of("EMAIL"))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.message").value("该主题为必达通知，站内信不可关闭"));

        // P2 不允许实时
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(payload("NOTICE_GENERAL", "REALTIME", List.of("INBOX"))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("该主题不支持实时通知，请选择 DAILY 或 WEEKLY"));

        // SMS 暂不可用
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(payload("BORROW_DUE", "DAILY", List.of("SMS"))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("渠道暂不可用：SMS，目前仅支持 INBOX、EMAIL"));

        // 非法频率
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(payload("BORROW_DUE", "HOURLY", List.of("INBOX"))))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.code").value(40000));

        // 未知主题
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(payload("NO_SUCH_TOPIC", "DAILY", List.of("INBOX"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400));

        // 空 items
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("items", List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("排程写入偏好快照，投递按快照渠道生效")
    void planUsesSnapshotChannels() throws Exception {
        // 读者 A：只留 EMAIL，站内信开关仍为默认开
        String emailOnly = newAccount("np");
        long emailOnlyId = createUser(emailOnly, "READER", "NORMAL", null);
        String tokenA = login(emailOnly);
        mockMvc.perform(put("/v1/me/notify-preferences")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenA))
                        .contentType(JSON)
                        .content(payload("BORROW_DUE", "DAILY", List.of("EMAIL"))))
                .andExpect(status().isOk());

        Fixture fixtureA = createBookWithCopy("ON_LOAN");
        long orderA = createActiveLoan(emailOnlyId, fixtureA);
        notifyService.planReturnReminders(orderA, emailOnlyId, LocalDate.now().plusDays(10));
        List<Long> tasksA = taskIds(orderA);
        assertThat(tasksA).hasSize(2);
        assertThat(jdbc.queryForList(
                        "SELECT topic_code FROM notify_task WHERE borrow_order_id = ?", String.class, orderA))
                .containsOnly("BORROW_DUE");
        assertThat(jdbc.queryForObject(
                        "SELECT JSON_UNQUOTE(JSON_EXTRACT(pref_snapshot, '$.channels[0]'))"
                                + " FROM notify_task WHERE borrow_order_id = ? LIMIT 1",
                        String.class,
                        orderA))
                .isEqualTo("EMAIL");

        // 读者 B：无自定义，默认 INBOX+EMAIL
        String plain = newAccount("np");
        long plainId = createUser(plain, "READER", "NORMAL", null);
        Fixture fixtureB = createBookWithCopy("ON_LOAN");
        long orderB = createActiveLoan(plainId, fixtureB);
        notifyService.planReturnReminders(orderB, plainId, LocalDate.now().plusDays(10));
        List<Long> tasksB = taskIds(orderB);
        assertThat(tasksB).hasSize(2);

        // 以 1 分钟前的时刻作为到期时间：JVM 与 MySQL 可能存在秒级时钟偏差，
        // 直接用 NOW() 可能被判为“未来”，导致偶发漏派发。
        jdbc.update("UPDATE notify_task SET plan_at = DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE borrow_order_id IN (?, ?)", orderA, orderB);
        notifyService.dispatchDue();

        assertThat(inboxCount(tasksA)).isZero();
        assertThat(inboxCount(tasksB)).isEqualTo(2);
        assertThat(jdbc.queryForList(
                        "SELECT status FROM notify_task WHERE borrow_order_id = ?", String.class, orderA))
                .containsOnly("SENT");
    }

    private String payload(String topicCode, String frequency, List<String> channels) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "items", List.of(Map.of("topicCode", topicCode, "frequency", frequency, "channels", channels))));
    }

    private List<Long> taskIds(long orderId) {
        return jdbc.queryForList(
                "SELECT id FROM notify_task WHERE borrow_order_id = ? ORDER BY level", Long.class, orderId);
    }

    private int inboxCount(List<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", taskIds.stream().map(id -> "?").toList());
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notify_delivery WHERE channel = 'INBOX' AND task_id IN (" + placeholders + ")",
                Integer.class,
                taskIds.toArray());
        return count == null ? 0 : count;
    }
}
