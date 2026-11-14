package com.zian.backend.reservation.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 读者端预约 DTO。
 */
public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record CreateRequest(@NotNull(message = "书目不能为空") Long bookId) {
    }

    public record ReservationItem(
            Long id,
            Long bookId,
            String bookTitle,
            String bookAuthor,
            int queuePosition,
            String status,
            String readyAt,
            String expiresAt) {
    }
}
