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
}
