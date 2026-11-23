package com.zian.backend.reservation.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.reservation.application.ReservationService;
import com.zian.backend.security.SecurityTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FR-05-D1 预约过期：手动过期释放占位，定时扫描顺延给队首。
 */
class ReservationExpiryTest extends SecurityTestSupport {

    @Autowired
    private ReservationService reservationService;

    @Test
    void manualExpireReleasesHeldCopyWhenQueueEmpty() throws Exception {
        long readerId = createUser(newAccount("rv"), "READER", "NORMAL", null);
        Fixture book = createBookWithCopy("READY_SHELF");
        long reservationId = createReservation(book.bookId(), readerId, 1, "READY", true);

        String librarianNo = newAccount("rl");
        createUser(librarianNo, "LIBRARIAN", "NORMAL", null);
        String token = login(librarianNo);

        mockMvc.perform(post("/v1/admin/reservations/{id}/expire", reservationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("EXPIRED"));

        assertThat(reservationStatus(reservationId)).isEqualTo("EXPIRED");
        assertThat(copyStatus(book.copyId())).isEqualTo("AVAILABLE");
    }

    @Test
    void scheduledScanExpiresAndPromotesNextWaiting() throws Exception {
        long firstReader = createUser(newAccount("r1"), "READER", "NORMAL", null);
        long nextReader = createUser(newAccount("r2"), "READER", "NORMAL", null);
        Fixture book = createBookWithCopy("READY_SHELF");
        long expiredId = createReservation(book.bookId(), firstReader, 1, "READY", true);
        long waitingId = createReservation(book.bookId(), nextReader, 2, "WAITING", false);

        reservationService.scanExpired();

        assertThat(reservationStatus(expiredId)).isEqualTo("EXPIRED");
        assertThat(reservationStatus(waitingId)).isEqualTo("READY");
        assertThat(copyStatus(book.copyId())).isEqualTo("READY_SHELF");
    }

    @Test
    void manualExpirePromotesNextWaitingWithoutReleasingCopy() throws Exception {
        long firstReader = createUser(newAccount("r1"), "READER", "NORMAL", null);
        long nextReader = createUser(newAccount("r2"), "READER", "NORMAL", null);
        Fixture book = createBookWithCopy("READY_SHELF");
        long expiredId = createReservation(book.bookId(), firstReader, 1, "READY", true);
        long waitingId = createReservation(book.bookId(), nextReader, 2, "WAITING", false);

        String librarianNo = newAccount("rl");
        createUser(librarianNo, "LIBRARIAN", "NORMAL", null);
        String token = login(librarianNo);

        mockMvc.perform(post("/v1/admin/reservations/{id}/expire", expiredId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXPIRED"));

        assertThat(reservationStatus(waitingId)).isEqualTo("READY");
        assertThat(copyStatus(book.copyId())).isEqualTo("READY_SHELF");
    }

    private long createReservation(long bookId, long readerId, int queueNo, String status, boolean expired) {
        Long id = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        LocalDateTime readyAt = expired ? LocalDateTime.now().minusHours(50) : null;
        LocalDateTime expiresAt = expired ? LocalDateTime.now().minusHours(2) : null;
        jdbc.update(
                """
                INSERT INTO reservation (id, book_id, reader_id, queue_no, status, ready_at, expires_at, version,
                                         created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW())
                """,
                id,
                bookId,
                readerId,
                queueNo,
                status,
                readyAt,
                expiresAt);
        return id;
    }

    private String reservationStatus(long id) {
        return jdbc.queryForObject("SELECT status FROM reservation WHERE id = ?", String.class, id);
    }

    private String copyStatus(long id) {
        return jdbc.queryForObject("SELECT status FROM book_copy WHERE id = ?", String.class, id);
    }
}
