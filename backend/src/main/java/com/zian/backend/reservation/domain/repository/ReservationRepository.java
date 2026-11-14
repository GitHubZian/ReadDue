package com.zian.backend.reservation.domain.repository;

import com.zian.backend.reservation.domain.model.Reservation;
import java.util.List;
import java.util.Optional;

/**
 * 预约仓储。
 */
public interface ReservationRepository {

    /**
     * 按 ID 查预约。
     */

    Optional<Reservation> findById(Long id);

    /**
     * 取该书目队首的等待中预约。
     */
    Optional<Reservation> findNextWaiting(Long bookId);

    /**
     * 查读者对同一书目的有效预约。
     */
    Optional<Reservation> findActiveByReaderAndBook(Long readerId, Long bookId);

    /**
     * 统计排队人数。
     */
    int countWaiting(Long bookId);

    /**
     * 取下一个排队序号。
     */
    int nextQueueNo(Long bookId);

    /**
     * 按读者分页查询预约。
     */

    List<Reservation> findByReader(Long readerId, String status, int offset, int limit);

    /**
     * 统计读者的预约数。
     */

    long countByReader(Long readerId, String status);

    /**
     * 新建预约，返回主键。
     */

    Long insert(Reservation reservation);

    /**
     * 回写预约状态与时间，返回影响行数。
     */

    int update(Reservation reservation);
}
