package com.zian.backend.reservation.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zian.backend.reservation.domain.model.Reservation;
import com.zian.backend.reservation.domain.model.ReservationStatus;
import com.zian.backend.reservation.domain.repository.ReservationRepository;
import com.zian.backend.reservation.infrastructure.persistence.mapper.ReservationMapper;
import com.zian.backend.reservation.infrastructure.persistence.po.ReservationPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 预约仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private static final List<String> ACTIVE =
            List.of(ReservationStatus.WAITING.name(), ReservationStatus.READY.name());

    private final ReservationMapper mapper;

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<Reservation> findNextWaiting(Long bookId) {
        ReservationPO po = mapper.selectOne(Wrappers.<ReservationPO>lambdaQuery()
                .eq(ReservationPO::getBookId, bookId)
                .eq(ReservationPO::getStatus, ReservationStatus.WAITING.name())
                .orderByAsc(ReservationPO::getQueueNo)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<Reservation> findExpiredReady(LocalDateTime now, int limit) {
        return mapper
                .selectList(Wrappers.<ReservationPO>lambdaQuery()
                        .eq(ReservationPO::getStatus, ReservationStatus.READY.name())
                        .lt(ReservationPO::getExpiresAt, now)
                        .orderByAsc(ReservationPO::getExpiresAt)
                        .last("limit " + Math.max(1, limit)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Reservation> findActiveByReaderAndBook(Long readerId, Long bookId) {
        ReservationPO po = mapper.selectOne(Wrappers.<ReservationPO>lambdaQuery()
                .eq(ReservationPO::getReaderId, readerId)
                .eq(ReservationPO::getBookId, bookId)
                .in(ReservationPO::getStatus, ACTIVE)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public int countWaiting(Long bookId) {
        return Math.toIntExact(mapper.selectCount(Wrappers.<ReservationPO>lambdaQuery()
                .eq(ReservationPO::getBookId, bookId)
                .eq(ReservationPO::getStatus, ReservationStatus.WAITING.name())));
    }

    @Override
    public int nextQueueNo(Long bookId) {
        ReservationPO last = mapper.selectOne(Wrappers.<ReservationPO>lambdaQuery()
                .eq(ReservationPO::getBookId, bookId)
                .orderByDesc(ReservationPO::getQueueNo)
                .last("limit 1"));
        return last == null || last.getQueueNo() == null ? 1 : last.getQueueNo() + 1;
    }

    @Override
    public List<Reservation> findByReader(Long readerId, String status, int offset, int limit) {
        return mapper
                .selectPage(
                        new Page<>(offset / Math.max(1, limit) + 1, limit, false),
                        Wrappers.<ReservationPO>lambdaQuery()
                                .eq(ReservationPO::getReaderId, readerId)
                                .eq(status != null && !status.isBlank(), ReservationPO::getStatus, status)
                                .orderByDesc(ReservationPO::getCreatedAt)
                                .orderByDesc(ReservationPO::getId))
                .getRecords()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByReader(Long readerId, String status) {
        return mapper.selectCount(Wrappers.<ReservationPO>lambdaQuery()
                .eq(ReservationPO::getReaderId, readerId)
                .eq(status != null && !status.isBlank(), ReservationPO::getStatus, status));
    }

    @Override
    public Long insert(Reservation reservation) {
        ReservationPO po = new ReservationPO();
        po.setBookId(reservation.getBookId());
        po.setReaderId(reservation.getReaderId());
        po.setQueueNo(reservation.getQueueNo());
        po.setStatus(reservation.getStatus().name());
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public int update(Reservation reservation) {
        ReservationPO po = new ReservationPO();
        po.setId(reservation.getId());
        po.setStatus(reservation.getStatus().name());
        po.setReadyAt(reservation.getReadyAt());
        po.setExpiresAt(reservation.getExpiresAt());
        po.setFulfilledAt(reservation.getFulfilledAt());
        po.setCancelReason(reservation.getCancelReason());
        po.setVersion(reservation.getVersion());
        return mapper.updateById(po);
    }

    private Reservation toDomain(ReservationPO po) {
        return new Reservation(
                po.getId(),
                po.getBookId(),
                po.getReaderId(),
                po.getQueueNo() == null ? 0 : po.getQueueNo(),
                ReservationStatus.valueOf(po.getStatus()),
                po.getReadyAt(),
                po.getExpiresAt(),
                po.getFulfilledAt(),
                po.getCancelReason(),
                po.getVersion());
    }
}
