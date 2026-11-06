package com.zian.backend.reservation.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.reservation.domain.model.Reservation;
import com.zian.backend.reservation.domain.model.ReservationStatus;
import com.zian.backend.reservation.domain.repository.ReservationRepository;
import com.zian.backend.reservation.infrastructure.persistence.mapper.ReservationMapper;
import com.zian.backend.reservation.infrastructure.persistence.po.ReservationPO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 预约仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

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
