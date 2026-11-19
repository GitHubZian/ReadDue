package com.zian.backend.notify.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * 免打扰：兜底值、局部更新、非法窗口、以及排程时按读者窗口顺延。
 */
class QuietHoursControllerTest extends SecurityTestSupport {

    @Test
    @DisplayName("未自定义时返回全局兜底 22:00–08:00")
    void fallbackWindow() throws Exception {
        String userNo = newAccount("qh");
        createUser(userNo, "READER", "NORMAL", null);

        mockMvc.perform(get("/v1/me/quiet-hours").header(HttpHeaders.AUTHORIZATION, bearer(login(userNo))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.start").value("22:00"))
                .andExpect(jsonPath("$.data.end").value("08:00"))
                .andExpect(jsonPath("$.data.source").value("FALLBACK"));
    }

    @Test
    @DisplayName("局部更新后标记为 CUSTOM 并可再次读取")
    void updateKeepsCustomWindow() throws Exception {
        String userNo = newAccount("qh");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(put("/v1/me/quiet-hours")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("start", "21:30", "end", "07:00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.start").value("21:30"))
                .andExpect(jsonPath("$.data.end").value("07:00"))
                .andExpect(jsonPath("$.data.source").value("CUSTOM"));

        mockMvc.perform(get("/v1/me/quiet-hours").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.data.start").value("21:30"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("起止相同与非法格式分别返回 422 和 400")
    void invalidWindowsRejected() throws Exception {
        String userNo = newAccount("qh");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(put("/v1/me/quiet-hours")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("start", "22:00", "end", "22:00"))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value(42200));

        mockMvc.perform(put("/v1/me/quiet-hours")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("start", "25:99"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("催还排程使用读者自定义窗口，命中后顺延到窗口结束")
    void schedulingUsesReaderWindow() throws Exception {
        String userNo = newAccount("qh");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        long orderId = createActiveLoan(readerId, createBookWithCopy("ON_LOAN"));

        mockMvc.perform(put("/v1/me/quiet-hours")
                        .header(HttpHeaders.AUTHORIZATION, bearer(login(userNo)))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("start", "00:01", "end", "23:59"))))
                .andExpect(status().isOk());

        String adminNo = newAccount("adm");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        mockMvc.perform(post("/v1/admin/remind/batch")
                        .header(HttpHeaders.AUTHORIZATION, bearer(login(adminNo)))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("ids", List.of(String.valueOf(orderId))))))
                .andExpect(status().isOk());

        Integer shifted = jdbc.queryForObject(
                "SELECT quiet_shifted FROM notify_task WHERE reader_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class,
                readerId);
        LocalTime planTime = jdbc.queryForObject(
                "SELECT TIME(plan_at) FROM notify_task WHERE reader_id = ? ORDER BY id DESC LIMIT 1",
                LocalTime.class,
                readerId);
        assertThat(shifted).isEqualTo(1);
        assertThat(planTime).isEqualTo(LocalTime.of(23, 59));
    }

    @Test
    @DisplayName("匿名读取免打扰返回 401")
    void anonymousRejected() throws Exception {
        mockMvc.perform(get("/v1/me/quiet-hours")).andExpect(status().isUnauthorized());
    }
}
