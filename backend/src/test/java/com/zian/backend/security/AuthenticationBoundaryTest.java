package com.zian.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * 认证 / 授权边界回归：401（未登录）、403（角色越权）、400（分页边界）。
 *
 * <p>断言只看业务码，不看中文文案，避免响应字符集差异带来的假失败。
 */
class AuthenticationBoundaryTest extends SecurityTestSupport {

    @Test
    void anonymousRequestToProtectedEndpointReturns401() throws Exception {
        mockMvc.perform(get("/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(get("/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void librarianIsForbiddenFromUserAdminEndpoint() throws Exception {
        String librarianNo = newAccount("T-LIB-");
        createUser(librarianNo, "LIBRARIAN", "NORMAL", null);
        String token = login(librarianNo);

        // /v1/admin/users 需要 user:blacklist，馆员只应拿到 403 + 40301。
        mockMvc.perform(get("/v1/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40301));
    }

    @Test
    void readerTokenCannotReachAdminUserList() throws Exception {
        String readerNo = newAccount("T-RD-");
        createUser(readerNo, "READER", "NORMAL", null);
        String token = login(readerNo);

        mockMvc.perform(get("/v1/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40301));
    }

    @Test
    void paginationBoundsAreRejectedWith400() throws Exception {
        String adminNo = newAccount("T-ADM-");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        String token = login(adminNo);

        mockMvc.perform(get("/v1/admin/users").param("page", "0").param("size", "10")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));

        mockMvc.perform(get("/v1/admin/users").param("page", "1").param("size", "201")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }
}
