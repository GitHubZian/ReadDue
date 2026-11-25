package com.zian.backend.reporting.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * M5-G：管理端审计 CSV 导出脱敏。
 */
class AuditExportMaskTest extends SecurityTestSupport {

    @Test
    void exportMasksPersonalFields() throws Exception {
        long id = IdWorker.getId();
        long adminId = jdbc.queryForObject(
                "SELECT id FROM sys_user WHERE user_no = 'ADMIN001'", Long.class);
        jdbc.update(
                """
                INSERT INTO audit_log (id, actor_id, actor_role, action, object_type, object_id, purpose,
                                       detail, ip, created_at)
                VALUES (?, ?, 'ADMIN', 'GRACE_APPROVE_MASK', 'GRACE_APPLICATION', 1, '导出脱敏测试',
                        CAST(? AS JSON), '10.1.2.3', NOW())
                """,
                id,
                adminId,
                "{\"name\":\"张三\",\"phone\":\"13812345678\",\"email\":\"a@b.com\",\"note\":\"通过\"}");

        String token = login("ADMIN001");
        MvcResult result = mockMvc.perform(get("/v1/admin/audit-logs/export")
                        .param("keyword", "GRACE_APPROVE_MASK")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        assertFalse(csv.contains("13812345678"), csv);
        assertFalse(csv.contains("a@b.com"), csv);
        assertFalse(csv.contains("10.1.2.3"), csv);
        assertFalse(csv.contains("张三"), csv);
        assertTrue(csv.contains("10.1.*.*"), csv);
        assertTrue(csv.contains("王**"), csv);
        assertEquals(2, csv.lines().count(), "表头 + 1 行数据");
    }
}
