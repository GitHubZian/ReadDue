package com.zian.backend.reservation.domain.repository;

import com.zian.backend.reservation.domain.model.Reservation;
import java.util.Optional;

/**
 * 预约仓储。
 */
public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    /** 该书目队首（queue_no 最小）的待书预约 */
    Optional<Reservation> findNextWaiting(Long bookId);

    int update(Reservation reservation);
}
