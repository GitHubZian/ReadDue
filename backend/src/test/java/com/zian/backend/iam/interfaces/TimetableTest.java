package com.zian.backend.iam.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * M5-F：读者课表与静默时段。
 */
class TimetableTest extends SecurityTestSupport {

    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 11, 25);

    @Test
    void saveReadAndQuietWindows() throws Exception {
        String userNo = newAccount("tt");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);
        int weekday = WEDNESDAY.getDayOfWeek().getValue();

        String body = objectMapper.writeValueAsString(Map.of("courses", List.of(
                Map.of("weekday", weekday, "startTime", "09:00", "endTime", "10:00"),
                Map.of("weekday", weekday, "startTime", "10:00", "endTime", "11:00"))));
        MvcResult saved = mockMvc.perform(put("/v1/me/timetable")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode savedNode = objectMapper.readTree(saved.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(0, savedNode.path("code").asInt(), savedNode.toString());
        assertTrue(savedNode.path("data").path("confirmed").asBoolean());
        assertEquals(2, savedNode.path("data").path("courses").size());
        assertFalse(savedNode.path("data").path("updatedAt").asText().isBlank());

        MvcResult read = mockMvc.perform(get("/v1/me/timetable").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode readNode = objectMapper.readTree(read.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(2, readNode.path("data").path("courses").size());
        assertEquals("09:00", readNode.path("data").path("courses").get(0).path("startTime").asText());

        MvcResult windows = mockMvc.perform(get("/v1/me/timetable/quiet-windows")
                        .param("date", WEDNESDAY.toString())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode windowsNode =
                objectMapper.readTree(windows.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(1, windowsNode.path("data").path("windows").size(), windowsNode.toString());
        assertEquals("08:45", windowsNode.path("data").path("windows").get(0).path("startTime").asText());
        assertEquals("11:15", windowsNode.path("data").path("windows").get(0).path("endTime").asText());
    }

    @Test
    void overlappingCoursesRejected() throws Exception {
        String userNo = newAccount("tt");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);
        String body = objectMapper.writeValueAsString(Map.of("courses", List.of(
                Map.of("weekday", 3, "startTime", "09:00", "endTime", "10:00"),
                Map.of("weekday", 3, "startTime", "09:30", "endTime", "10:30"))));
        MvcResult result = mockMvc.perform(put("/v1/me/timetable")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(body))
                .andExpect(status().is(422))
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(42200, node.path("code").asInt());
    }

    @Test
    void invalidWeekdayAndTooManyCoursesRejected() throws Exception {
        String userNo = newAccount("tt");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);
        String badWeekday = objectMapper.writeValueAsString(Map.of("courses", List.of(
                Map.of("weekday", 8, "startTime", "09:00", "endTime", "10:00"))));
        mockMvc.perform(put("/v1/me/timetable")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(badWeekday))
                .andExpect(status().isBadRequest());

        List<Map<String, Object>> many = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            many.add(Map.of("weekday", 1, "startTime", "08:00", "endTime", "08:01"));
        }
        String tooMany = objectMapper.writeValueAsString(Map.of("courses", many));
        mockMvc.perform(put("/v1/me/timetable")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(tooMany))
                .andExpect(status().isBadRequest());
    }
}
