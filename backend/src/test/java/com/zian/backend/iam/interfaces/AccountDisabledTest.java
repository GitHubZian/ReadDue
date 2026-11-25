package com.zian.backend.iam.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 注销后账号立即终止：旧令牌失效、不能再登录、不能再改资料。
 */
class AccountDisabledTest extends SecurityTestSupport {

    @Test
    void forgottenAccountRejectsEveryEntry() throws Exception {
        String userNo = newAccount("dis");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(post("/v1/me/forget").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/me").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());

        String body = objectMapper.writeValueAsString(Map.of("userNo", userNo, "password", DEMO_PASSWORD));
        mockMvc.perform(post("/v1/auth/login").contentType(JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message").value("账号已注销"));

        mockMvc.perform(put("/v1/me")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content("{\"name\":\"新名字\"}".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isUnauthorized());
    }
}
