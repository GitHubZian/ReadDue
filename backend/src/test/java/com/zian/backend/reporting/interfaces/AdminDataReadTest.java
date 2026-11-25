package com.zian.backend.reporting.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * M6-①：管理端查看用户敏感详情留痕，且读取「读取记录」本身不再留痕。
 */
class AdminDataReadTest extends SecurityTestSupport {

    @Test
    void userDetailRecordsDataReadOnlyOnce() throws Exception {
        String userNo = newAccount("dr");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        String admin = bearer(login("ADMIN001"));

        mockMvc.perform(get("/v1/admin/users/{id}", readerId).header("Authorization", admin))
                .andExpect(status().isOk());

        JsonNode first = dataReads(admin, readerId);
        assertThat(first.size()).isEqualTo(1);
        assertThat(first.get(0).path("operatorName").asText()).isEqualTo("王敏");
        assertThat(first.get(0).path("targetType").asText()).isEqualTo("USER");
        assertThat(first.get(0).path("targetId").asText()).isEqualTo(String.valueOf(readerId));

        // 查询 data-reads 是只读接口，不应产生新的 DATA_READ。
        JsonNode second = dataReads(admin, readerId);
        assertThat(second.size()).isEqualTo(1);
    }

    private JsonNode dataReads(String admin, long readerId) throws Exception {
        String body = mockMvc.perform(get("/v1/admin/data-reads")
                        .param("targetType", "USER")
                        .param("targetId", String.valueOf(readerId))
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("data");
    }
}
