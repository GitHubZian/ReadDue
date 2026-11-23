package com.zian.backend.iam.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * {@code GET /v1/me} 资料字段与累计借阅回归。
 */
class MeProfileTest extends SecurityTestSupport {

    @Test
    void meReturnsProfileAndTotalBorrowedIncludingReturned() throws Exception {
        String userNo = newAccount("me");
        long readerId = createUser(userNo, "READER", "NORMAL", null);
        jdbc.update(
                "INSERT INTO reader_profile (user_id, department, major, grade) VALUES (?, ?, ?, ?)",
                readerId,
                "计算机学院",
                "软件工程",
                "2023");
        createActiveLoan(readerId, createBookWithCopy("ON_LOAN"));
        long returnedId = createActiveLoan(readerId, createBookWithCopy("AVAILABLE"));
        jdbc.update("UPDATE borrow_order SET status = 'RETURNED', returned_at = NOW() WHERE id = ?", returnedId);

        String token = login(userNo);
        mockMvc.perform(get("/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.studentNo").value(userNo))
                .andExpect(jsonPath("$.data.role").value("READER"))
                .andExpect(jsonPath("$.data.department").value("计算机学院"))
                .andExpect(jsonPath("$.data.major").value("软件工程"))
                .andExpect(jsonPath("$.data.grade").value("2023"))
                .andExpect(jsonPath("$.data.totalBorrowed").value(2));
    }

    @Test
    void meDefaultsToEmptyProfileAndZeroBorrowed() throws Exception {
        String userNo = newAccount("me0");
        createUser(userNo, "READER", "NORMAL", null);

        String token = login(userNo);
        mockMvc.perform(get("/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department").value(""))
                .andExpect(jsonPath("$.data.major").value(""))
                .andExpect(jsonPath("$.data.grade").value(""))
                .andExpect(jsonPath("$.data.totalBorrowed").value(0));
    }
}
