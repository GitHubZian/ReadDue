package com.zian.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 黑名单准入闸回归：借阅被 422 拒绝，但归还（以及申诉等读者自助动作）不因拉黑被拦截。
 *
 * <p>这是产品约定：拉黑只剥夺「新增占用馆藏」的能力，不能阻断读者履行归还义务，
 * 否则逾期会无限累积。用例自己插入黑名单读者与在借单，结束后随事务回滚。
 */
class BlacklistBorrowGateTest extends SecurityTestSupport {

    @Test
    void blacklistedReaderCannotBorrow() throws Exception {
        String readerNo = newAccount("T-BL-");
        createUser(readerNo, "READER", "BLACKLISTED", "安全审计：黑名单借阅拦截");
        String token = login(readerNo);
        Fixture fixture = createBookWithCopy("AVAILABLE");

        mockMvc.perform(post("/v1/borrows")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("readerNo", readerNo, "barcode", fixture.barcode()))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42200));
    }

    @Test
    void blacklistedReaderCanStillReturn() throws Exception {
        String readerNo = newAccount("T-BL-");
        long readerId = createUser(readerNo, "READER", "BLACKLISTED", "安全审计：黑名单归还放行");
        String token = login(readerNo);
        Fixture fixture = createBookWithCopy("ON_LOAN");
        createActiveLoan(readerId, fixture);

        mockMvc.perform(post("/v1/returns")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("barcode", fixture.barcode()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 归还后借阅单应闭合为 RETURNED，馆藏回到 AVAILABLE。
        String orderStatus = jdbc.queryForObject(
                "SELECT status FROM borrow_order WHERE copy_id = ? ORDER BY id DESC LIMIT 1",
                String.class,
                fixture.copyId());
        String copyStatus = jdbc.queryForObject(
                "SELECT status FROM book_copy WHERE id = ?", String.class, fixture.copyId());
        org.assertj.core.api.Assertions.assertThat(orderStatus).isEqualTo("RETURNED");
        org.assertj.core.api.Assertions.assertThat(copyStatus).isEqualTo("AVAILABLE");
    }
}
