package com.zian.backend.notify.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * 渠道开关：默认值、局部更新、关闭邮件后不再产生 EMAIL 投递。
 */
class ReaderPreferenceControllerTest extends SecurityTestSupport {

    @Test
    @DisplayName("未建档的读者两个渠道都默认开启")
    void defaultsAreOn() throws Exception {
        String userNo = newAccount("pref");
        createUser(userNo, "READER", "NORMAL", null);

        mockMvc.perform(get("/v1/me/channels").header(HttpHeaders.AUTHORIZATION, bearer(login(userNo))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(true))
                .andExpect(jsonPath("$.data.inbox").value(true));
    }

    @Test
    @DisplayName("关闭邮件后催还只落站内信，不再入队 EMAIL")
    void emailOffSkipsEmailDelivery() throws Exception {
        String userNo = newAccount("pref");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        String token = login(userNo);

        mockMvc.perform(put("/v1/me/channels")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updated").value(1))
                .andExpect(jsonPath("$.data.channels.email").value(false))
                .andExpect(jsonPath("$.data.channels.inbox").value(true));

        mockMvc.perform(get("/v1/me/channels").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.data.email").value(false));

        String adminNo = newAccount("adm");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        mockMvc.perform(post("/v1/admin/remind/batch")
                        .header(HttpHeaders.AUTHORIZATION, bearer(login(adminNo)))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("ids", java.util.List.of(String.valueOf(orderId))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(1));

        assertThat(deliveryCount(readerId, "EMAIL")).isZero();
        assertThat(deliveryCount(readerId, "INBOX")).isEqualTo(1);
    }

    @Test
    @DisplayName("两个字段都不传时拒绝，避免空请求被当成全部关闭")
    void emptyUpdateRejected() throws Exception {
        String userNo = newAccount("pref");
        createUser(userNo, "READER", "NORMAL", null);

        mockMvc.perform(put("/v1/me/channels")
                        .header(HttpHeaders.AUTHORIZATION, bearer(login(userNo)))
                        .contentType(JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("匿名读取渠道开关返回 401")
    void anonymousRejected() throws Exception {
        mockMvc.perform(get("/v1/me/channels")).andExpect(status().isUnauthorized());
    }

    private int deliveryCount(long readerId, String channel) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM notify_delivery d
                  JOIN notify_task t ON t.id = d.task_id
                 WHERE t.reader_id = ? AND d.channel = ?
                """,
                Integer.class,
                readerId,
                channel);
        return count == null ? 0 : count;
    }
}

