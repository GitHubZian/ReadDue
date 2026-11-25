package com.zian.backend.reporting.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * M6-③：手机号仅支持 11 位精确匹配，部分号码不再按手机号模糊命中。
 */
class AdminUserSearchTest extends SecurityTestSupport {

    @Test
    void phoneKeywordOnlyMatchesExactNumber() throws Exception {
        long first = createUser(newAccount("ph"), "READER", "NORMAL", null);
        long second = createUser(newAccount("ph"), "READER", "NORMAL", null);
        jdbc.update("UPDATE sys_user SET phone = '13700000011' WHERE id = ?", first);
        jdbc.update("UPDATE sys_user SET phone = '13600000012' WHERE id = ?", second);
        String admin = bearer(login("ADMIN001"));

        JsonNode exact = search(admin, "13700000011");
        assertThat(exact.path("total").asInt()).isEqualTo(1);
        assertThat(exact.path("items").get(0).path("id").asText()).isEqualTo(String.valueOf(first));

        // 7 位片段不是手机号，也不再命中 phone。
        assertThat(search(admin, "1370000").path("total").asInt()).isZero();

        // 姓名/学号仍可按关键字模糊匹配。
        assertThat(search(admin, "安全审计").path("total").asInt()).isGreaterThanOrEqualTo(2);
    }

    private JsonNode search(String admin, String keyword) throws Exception {
        String body = mockMvc.perform(get("/v1/admin/users")
                        .param("keyword", keyword)
                        .param("size", "50")
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("data");
    }
}
