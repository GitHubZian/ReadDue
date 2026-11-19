package com.zian.backend.borrow.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * 读者首页摘要与借阅配额：计数口径、读者隔离、配额拦截。
 *
 * <p>用例在事务内插入自己的读者与借阅单并回滚，不依赖演示数据的当前状态。
 */
class ReaderSummaryControllerTest extends SecurityTestSupport {

    @Test
    @DisplayName("摘要按口径统计在借/临期/逾期/学期归还，并带出配额与信用档位")
    void summaryShouldAggregateCounts() throws Exception {
        String userNo = newAccount("sum");
        long readerId = createUser(userNo, "READER", "NORMAL", null);

        Fixture dueSoon = createBookWithCopy("ON_LOAN");
        Fixture later = createBookWithCopy("ON_LOAN");
        Fixture overdue = createBookWithCopy("ON_LOAN");
        Fixture returned = createBookWithCopy("AVAILABLE");
        Fixture oldReturned = createBookWithCopy("AVAILABLE");

        insertLoan(readerId, dueSoon, LocalDate.now().plusDays(2), null, "BORROWED");
        insertLoan(readerId, later, LocalDate.now().plusDays(30), null, "BORROWED");
        insertLoan(readerId, overdue, LocalDate.now().minusDays(5), null, "OVERDUE");
        insertLoan(readerId, returned, LocalDate.now().minusDays(60), LocalDate.now().minusDays(1), "RETURNED");
        insertLoan(readerId, oldReturned, LocalDate.now().minusDays(700), LocalDate.now().minusDays(600), "RETURNED");

        jdbc.update(
                "INSERT INTO reader_credit (user_id, kt_tier, overdue_cnt_12m) VALUES (?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE kt_tier = VALUES(kt_tier)",
                readerId,
                2,
                1);

        String token = login(userNo);
        String body = mockMvc.perform(get("/v1/me/summary").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.borrowed").value(3))
                .andExpect(jsonPath("$.data.dueIn3Days").value(1))
                .andExpect(jsonPath("$.data.overdue").value(1))
                .andExpect(jsonPath("$.data.quota").value(5))
                .andExpect(jsonPath("$.data.returnedThisTerm").value(1))
                .andExpect(jsonPath("$.data.creditLevel").value(2))
                .andExpect(jsonPath("$.data.creditLabel").value("一般"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).contains("\"borrowed\":3");
    }

    @Test
    @DisplayName("摘要只统计本人，看不到他人的在借")
    void summaryShouldIgnoreOtherReaders() throws Exception {
        String userNo = newAccount("me");
        createUser(userNo, "READER", "NORMAL", null);
        long otherId = createUser(newAccount("other"), "READER", "NORMAL", null);
        Fixture otherLoan = createBookWithCopy("ON_LOAN");
        insertLoan(otherId, otherLoan, LocalDate.now().minusDays(3), null, "OVERDUE");

        String token = login(userNo);
        mockMvc.perform(get("/v1/me/summary").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.borrowed").value(0))
                .andExpect(jsonPath("$.data.overdue").value(0));
    }

    @Test
    @DisplayName("达到配额后借出被拒，提示上限")
    void borrowShouldRejectWhenQuotaReached() throws Exception {
        String userNo = newAccount("quota");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        for (int i = 0; i < 5; i++) {
            Fixture held = createBookWithCopy("ON_LOAN");
            insertLoan(readerId, held, LocalDate.now().plusDays(20), null, "BORROWED");
        }
        Fixture extra = createBookWithCopy("AVAILABLE");

        String token = login(userNo);
        String payload = objectMapper.writeValueAsString(Map.of("readerNo", userNo, "barcode", extra.barcode()));
        mockMvc.perform(post("/v1/borrows")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(JSON)
                        .content(payload))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.message").value("已达借阅上限（5 本），请先归还"));
    }

    @Test
    @DisplayName("匿名访问摘要返回 401")
    void summaryRequiresLogin() throws Exception {
        mockMvc.perform(get("/v1/me/summary")).andExpect(status().isUnauthorized());
    }

    private void insertLoan(
            long readerId, Fixture fixture, LocalDate dueAt, LocalDate returnedAt, String status) {
        jdbc.update(
                """
                INSERT INTO borrow_order (id, order_no, reader_id, book_id, copy_id, borrowed_at, due_at,
                                          effective_due_at, returned_at, status, final_overdue_days)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """,
                IdWorker.getId(),
                "TS" + IdWorker.getId(),
                readerId,
                fixture.bookId(),
                fixture.copyId(),
                dueAt.minusDays(30).atStartOfDay(),
                dueAt,
                dueAt,
                returnedAt == null ? null : returnedAt.atStartOfDay(),
                status);
    }
}

