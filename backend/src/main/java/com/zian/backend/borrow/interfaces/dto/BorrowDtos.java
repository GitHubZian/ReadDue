package com.zian.backend.borrow.interfaces.dto;

import com.zian.backend.shared.dto.BookRefView;
import com.zian.backend.shared.dto.PersonView;
import jakarta.validation.constraints.NotBlank;

/**
 * 借还台 DTO。
 */
public final class BorrowDtos {

    private BorrowDtos() {
    }

    public record BorrowRequest(
            @NotBlank(message = "学号/工号不能为空") String readerNo,
            @NotBlank(message = "条码不能为空") String barcode) {
    }

    public record ReturnRequest(@NotBlank(message = "条码不能为空") String barcode) {
    }

    public record BorrowResult(Long borrowId, PersonView reader, BookRefView book, String dueAt) {
    }

    public record ReturnResult(BookRefView book, PersonView reader, String fine, int overdueDays) {
    }

    public record RenewResult(
            Long borrowId, String dueAt, String effectiveDueAt, int renewCount, int maxRenew) {
    }

    /** 读者端「我的借阅」条目。 */
    public record BorrowItem(
            Long id,
            String code,
            String bookTitle,
            String bookAuthor,
            String coverUrl,
            String borrowedAt,
            java.time.LocalDate dueAt,
            java.time.LocalDate effectiveDueAt,
            String status,
            int renewCount,
            int graceDays,
            int overdueDays) {
    }
}
