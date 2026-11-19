package com.zian.backend.rule.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.rule.application.AcademicCalendarService;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 校历写接口：新建 / 修改 / 删除与校验、审计。
 */
class CalendarControllerTest extends SecurityTestSupport {

    private static final String BASE = "/v1/admin/calendar";

    @Autowired
    private AcademicCalendarService academicCalendarService;

    @Test
    void semesterStartShouldUseNormalRange() {
        jdbc.update(
                "INSERT INTO academic_calendar (id, term, scene_code, start_date, end_date, source, deleted)"
                        + " VALUES (?, ?, ?, ?, ?, ?, 0)",
                IdWorker.getId(),
                "2026-FALL",
                "NORMAL",
                java.sql.Date.valueOf("2026-09-01"),
                java.sql.Date.valueOf("2027-01-31"),
                "DECLARED");

        assertThat(academicCalendarService.semesterStart(LocalDate.of(2026, 10, 15)))
                .contains(LocalDate.of(2026, 9, 1));
        assertThat(academicCalendarService.semesterStart(LocalDate.of(2030, 1, 1))).isEmpty();
    }

    @Test
    void createShouldPersistAndAudit() throws Exception {
        String token = login("ADMIN001");

        String body = mockMvc.perform(post(BASE)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2027-SPRING",
                                "scene", "EXAM",
                                "startDate", "2027-01-05",
                                "endDate", "2027-01-18",
                                "note", "期末考试"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.term").value("2027-SPRING"))
                .andExpect(jsonPath("$.data.scene").value("EXAM"))
                .andExpect(jsonPath("$.data.startDate").value("2027-01-05"))
                .andExpect(jsonPath("$.data.source").value("DECLARED"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        String id = objectMapper.readTree(body).path("data").path("id").asText();
        Integer audits = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action = 'CALENDAR_CREATE' AND object_id = ?",
                Integer.class,
                Long.parseLong(id));
        assertThat(audits).isEqualTo(1);
    }

    @Test
    void invalidTermOrSceneShouldBe400() throws Exception {
        String token = login("ADMIN001");

        mockMvc.perform(post(BASE)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2026春", "scene", "EXAM", "startDate", "2026-09-01", "endDate", "2026-09-02"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));

        mockMvc.perform(post(BASE)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2026-FALL", "scene", "HOLIDAY", "startDate", "2026-09-01", "endDate", "2026-09-02"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void reversedRangeShouldBe422() throws Exception {
        String token = login("ADMIN001");

        mockMvc.perform(post(BASE)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2026-FALL", "scene", "EXAM", "startDate", "2026-09-20", "endDate", "2026-09-01"))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.message").value("开始日期不能晚于结束日期"));
    }

    @Test
    void overlappingRangeShouldBe422ButSelfUpdateAllowed() throws Exception {
        String token = login("ADMIN001");
        long id = insertRange("2026-FALL", "THESIS", "2026-12-01", "2026-12-15");

        mockMvc.perform(post(BASE)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2026-FALL", "scene", "THESIS", "startDate", "2026-12-10", "endDate", "2026-12-20"))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("该学期同一场景在所选区间已有校历"));

        mockMvc.perform(put(BASE + "/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2026-FALL",
                                "scene", "THESIS",
                                "startDate", "2026-12-01",
                                "endDate", "2026-12-15",
                                "note", "改备注"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.note").value("改备注"));

        mockMvc.perform(put(BASE + "/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2026-FALL", "scene", "THESIS", "startDate", "2026-12-01", "endDate", "2026-12-31"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endDate").value("2026-12-31"));
    }

    @Test
    void deleteThenUpdateShouldBe404() throws Exception {
        String token = login("ADMIN001");
        long id = insertRange("2027-FALL", "NORMAL", "2027-09-01", "2027-09-30");

        mockMvc.perform(delete(BASE + "/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete(BASE + "/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400));

        mockMvc.perform(put(BASE + "/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "term", "2027-FALL", "scene", "NORMAL", "startDate", "2027-09-01", "endDate", "2027-09-30"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void readerShouldBeForbiddenAndAnonymousUnauthorized() throws Exception {
        String reader = login("2021001");
        String payload = objectMapper.writeValueAsString(Map.of(
                "term", "2028-SPRING", "scene", "NORMAL", "startDate", "2028-02-20", "endDate", "2028-06-30"));

        mockMvc.perform(post(BASE).header("Authorization", bearer(reader)).contentType(JSON).content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(BASE).contentType(JSON).content(payload)).andExpect(status().isUnauthorized());
    }

    private long insertRange(String term, String scene, String start, String end) {
        long id = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        jdbc.update(
                """
                INSERT INTO academic_calendar (id, term, scene_code, start_date, end_date, source, note, deleted, created_at)
                VALUES (?, ?, ?, ?, ?, 'DECLARED', NULL, 0, NOW())
                """,
                id,
                term,
                scene,
                start,
                end);
        return id;
    }
}
