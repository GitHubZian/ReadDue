package com.zian.backend.appeal.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zian.backend.appeal.domain.model.AppealStatus;
import com.zian.backend.appeal.domain.model.AppealTicket;
import com.zian.backend.appeal.domain.repository.AppealTicketRepository;
import com.zian.backend.appeal.infrastructure.persistence.mapper.AppealTicketMapper;
import com.zian.backend.appeal.infrastructure.persistence.po.AppealTicketPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 申诉工单仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class AppealTicketRepositoryImpl implements AppealTicketRepository {

    private static final List<String> OPEN_STATUS = List.of(AppealStatus.OPEN.name(), AppealStatus.ACKED.name());

    private final AppealTicketMapper mapper;

    @Override
    public Optional<AppealTicket> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<AppealTicket> findSlaDue(LocalDateTime now, int limit) {
        return mapper.selectList(Wrappers.<AppealTicketPO>lambdaQuery()
                        .in(AppealTicketPO::getStatus, OPEN_STATUS)
                        .eq(AppealTicketPO::getSlaBreached, 0)
                        .lt(AppealTicketPO::getSlaDueAt, now)
                        .orderByAsc(AppealTicketPO::getSlaDueAt)
                        .last("limit " + Math.max(limit, 1)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int update(AppealTicket ticket) {
        AppealTicketPO po = new AppealTicketPO();
        po.setId(ticket.getId());
        po.setStatus(ticket.getStatus().name());
        po.setFirstReplyAt(ticket.getFirstReplyAt());
        po.setSlaBreached(ticket.isSlaBreached() ? 1 : 0);
        po.setAssigneeId(ticket.getAssigneeId());
        po.setAssigneeName(ticket.getAssigneeName());
        po.setCompensationDays(ticket.getCompensationDays());
        po.setResolution(ticket.getResolution());
        return mapper.updateById(po);
    }

    @Override
    public Long insert(AppealTicket ticket) {
        AppealTicketPO po = new AppealTicketPO();
        po.setTicketNo(ticket.getTicketNo());
        po.setReaderId(ticket.getReaderId());
        po.setTargetType(ticket.getTargetType());
        po.setTargetId(ticket.getTargetId());
        po.setReason(ticket.getReason());
        po.setStatus(ticket.getStatus().name());
        po.setSlaDueAt(ticket.getSlaDueAt());
        po.setSlaBreached(ticket.isSlaBreached() ? 1 : 0);
        po.setCompensationDays(ticket.getCompensationDays());
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public List<AppealTicket> findByReader(Long readerId, String status, int offset, int limit) {
        return mapper
                .selectPage(
                        new Page<>(offset / Math.max(1, limit) + 1, limit, false),
                        Wrappers.<AppealTicketPO>lambdaQuery()
                                .eq(AppealTicketPO::getReaderId, readerId)
                                .eq(status != null && !status.isBlank(), AppealTicketPO::getStatus, status)
                                .orderByDesc(AppealTicketPO::getCreatedAt)
                                .orderByDesc(AppealTicketPO::getId))
                .getRecords()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByReader(Long readerId, String status) {
        return mapper.selectCount(Wrappers.<AppealTicketPO>lambdaQuery()
                .eq(AppealTicketPO::getReaderId, readerId)
                .eq(status != null && !status.isBlank(), AppealTicketPO::getStatus, status));
    }

    private AppealTicket toDomain(AppealTicketPO po) {
        return new AppealTicket(
                po.getId(),
                po.getTicketNo(),
                po.getReaderId(),
                po.getTargetType(),
                po.getTargetId(),
                po.getReason(),
                AppealStatus.valueOf(po.getStatus()),
                po.getSlaDueAt(),
                po.getFirstReplyAt(),
                po.getSlaBreached() != null && po.getSlaBreached() == 1,
                po.getAssigneeId(),
                po.getAssigneeName(),
                po.getCompensationDays() == null ? 0 : po.getCompensationDays(),
                po.getResolution(),
                po.getCreatedAt());
    }
}
