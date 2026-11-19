package com.zian.backend.catalog.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 读者端书目检索接口回归：空结果、分页上限、详情/馆藏/队列共用库存口径。
 */
@DisplayName("书目检索与馆藏接口")
class BookControllerTest extends SecurityTestSupport {

    @Test
    @DisplayName("无匹配关键词时返回空页而不是 404")
    void searchReturnsEmptyPageWhenNothingMatches() throws Exception {
        String userNo = newAccount("book");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(get("/v1/books")
                        .param("q", "无此书名-" + UUID.randomUUID())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    @DisplayName("size 超过 200 与全站一致返回 400，而不是静默截断")
    void searchRejectsOversizedPage() throws Exception {
        String userNo = newAccount("book");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(get("/v1/books")
                        .param("size", "201")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("详情、馆藏、队列都以 v_book_stock 为库存口径")
    void detailCopiesAndQueueShareStockView() throws Exception {
        String userNo = newAccount("book");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);
        Fixture fixture = createBookWithCopy("AVAILABLE");

        mockMvc.perform(get("/v1/books/{id}", fixture.bookId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("安全审计测试书目"))
                .andExpect(jsonPath("$.data.onShelf").value(true))
                .andExpect(jsonPath("$.data.totalCopies").value(1))
                .andExpect(jsonPath("$.data.availableCopies").value(1))
                .andExpect(jsonPath("$.data.onLoanCopies").value(0));

        mockMvc.perform(get("/v1/books/{id}/copies", fixture.bookId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].barcode").value(fixture.barcode()))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));

        mockMvc.perform(get("/v1/books/{id}/queue", fixture.bookId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.waiting").value(0))
                .andExpect(jsonPath("$.data.myQueueNo").doesNotExist());
    }

    @Test
    @DisplayName("不存在的书目返回 404")
    void detailReturnsNotFoundForUnknownBook() throws Exception {
        String userNo = newAccount("book");
        createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        mockMvc.perform(get("/v1/books/{id}", 1L)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400));
    }
}
