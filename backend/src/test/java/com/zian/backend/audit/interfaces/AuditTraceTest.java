package com.zian.backend.audit.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

/**
 * B-09 审计可追溯：traceId 与前后值落库、接口回显。
 *
 * <p>覆盖三类留痕写操作中的两类（规则调整、宽限审批）；申诉处理走同一 {@code AuditRecorder} 重载，
 * 由 {@code AdminQueryMapperSmokeTest} 的字段映射覆盖读取链路。
 */
class AuditTraceTest extends SecurityTestSupport {

    private static final String TRACE = "trace-b09-0001";

    @Test
    void ruleUpdateRecordsTraceAndBeforeAfter() throws Exception {
        String adminNo = newAccount("auditadm");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        String token = login(adminNo);

        String version = currentRuleVersion(token);
        String payload = objectMapper.writeValueAsString(Map.of(
                "expectedVersion",
                version,
                "groups",
                List.of(Map.of(
                        "code",
                        "FINE",
                        "items",
                        List.of(Map.of("key", "FINE.PER_DAY", "value", "0.60", "type", "money"))))));

        mockMvc.perform(put("/v1/admin/rules")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .header("X-Trace-Id", TRACE)
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT trace_id, before_json, after_json FROM audit_log "
                        + "WHERE action = 'RULE_UPDATE' ORDER BY created_at DESC, id DESC LIMIT 1");
        assertThat(row.get("trace_id")).isEqualTo(TRACE);
        assertThat(String.valueOf(row.get("before_json"))).contains("FINE.PER_DAY").contains("0.50");
        assertThat(String.valueOf(row.get("after_json"))).contains("FINE.PER_DAY").contains("0.60");

        JsonNode list = getJson("/v1/admin/audit-logs?keyword=RULE_UPDATE&page=1&size=10", token);
        JsonNode item = null;
        for (JsonNode node : list.path("data").path("items")) {
            if ("RULE_UPDATE".equals(node.path("action").asText())) {
                item = node;
                break;
            }
        }
        assertThat(item).as("列表应包含本次规则调整").isNotNull();
        assertThat(item.path("traceId").asText()).isEqualTo(TRACE);
        assertThat(item.path("before").asText()).contains("FINE.PER_DAY");
        assertThat(item.path("after").asText()).contains("0.60");

        String id = item.path("id").asText();
        JsonNode detail = getJson("/v1/admin/audit-logs/" + id, token);
        assertThat(detail.path("data").path("traceId").asText()).isEqualTo(TRACE);
        assertThat(detail.path("data").path("before").asText()).contains("0.50");
    }

    @Test
    void graceApproveRecordsStatusTransition() throws Exception {
        String readerNo = newAccount("auditrd");
        long readerId = createUser(readerNo, "READER", "NORMAL", null);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, fixture);
        long graceId = insertPendingGrace(orderId, readerId);

        String adminNo = newAccount("auditadm2");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        String token = login(adminNo);

        String payload = objectMapper.writeValueAsString(Map.of("days", 3, "note", "B-09 冒烟"));
        mockMvc.perform(post("/v1/admin/graces/" + graceId + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .header("X-Trace-Id", TRACE + "-grace")
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT trace_id, before_json, after_json FROM audit_log "
                        + "WHERE action = 'GRACE_APPROVE' ORDER BY created_at DESC, id DESC LIMIT 1");
        assertThat(row.get("trace_id")).isEqualTo(TRACE + "-grace");
        assertThat(String.valueOf(row.get("before_json"))).contains("PENDING");
        assertThat(String.valueOf(row.get("after_json"))).contains("APPROVED").contains("grantedDays");
    }

    private String currentRuleVersion(String token) throws Exception {
        JsonNode body = getJson("/v1/admin/rules", token);
        return body.path("data").path("version").asText();
    }

    private JsonNode getJson(String url, String token) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long insertPendingGrace(long orderId, long readerId) {
        long id = IdWorker.getId();
        jdbc.update(
                """
                INSERT INTO grace_application (id, apply_no, borrow_order_id, reader_id, scene_code, scene_source,
                        calendar_hit, base_days, kt, kq, dmax, computed_days, evidence_text, formula_version,
                        queue_len, snapshot_expires_at, status, version)
                VALUES (?, ?, ?, ?, 'EXAM', 'DECLARED', 0, 3.0, 1.00, 1.00, 7, 3.0, '审计测试', 'v1',
                        0, DATE_ADD(NOW(), INTERVAL 24 HOUR), 'PENDING', 0)
                """,
                id,
                "TA" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                orderId,
                readerId);
        return id;
    }
}
