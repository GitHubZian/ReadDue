package com.zian.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 规则参数乐观锁回归：客户端携带过期 {@code expectedVersion} 时必须 409，而不是静默覆盖。
 *
 * <p>规则参数是全站借阅/宽限/通知的公共配置，版本冲突静默成功会造成并发写互相覆盖。
 */
class RuleVersionConflictTest extends SecurityTestSupport {

    @Test
    void staleRuleVersionIsRejectedWith409() throws Exception {
        String adminNo = newAccount("T-ADM-");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        String token = login(adminNo);

        String payload = objectMapper.writeValueAsString(
                Map.of("groups", List.of(), "expectedVersion", "v-stale-" + System.nanoTime()));

        mockMvc.perform(put("/v1/admin/rules")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900));
    }
}
