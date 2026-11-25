package com.zian.backend.reporting.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * 用户列表默认脱敏手机号/邮箱；详情仍由 DATA_READ 审计后返回原值。
 */
class AdminUserMaskTest extends SecurityTestSupport {

    @Test
    void listMasksPhoneAndEmail() throws Exception {
        String userNo = newAccount("mask");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        jdbc.update(
                "UPDATE sys_user SET phone = ?, email = ? WHERE id = ?",
                "13812345678",
                "zhangsan@example.com",
                readerId);
        String admin = login("ADMIN001");

        String listBody = mockMvc.perform(get("/v1/admin/users")
                        .param("keyword", userNo)
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].phone").value("138****5678"))
                .andExpect(jsonPath("$.data.items[0].email").value("z***@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(listBody).doesNotContain("13812345678");

        mockMvc.perform(get("/v1/admin/users/{id}", readerId).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("13812345678"));
    }
}
