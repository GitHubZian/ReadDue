package com.zian.backend.shared.outbox.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zian.backend.shared.outbox.domain.model.OutboxEvent;
import com.zian.backend.shared.outbox.domain.model.OutboxStatus;
import com.zian.backend.shared.outbox.domain.repository.OutboxRepository;
import com.zian.backend.shared.outbox.infrastructure.persistence.mapper.EventOutboxMapper;
import com.zian.backend.shared.outbox.infrastructure.persistence.po.EventOutboxPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 本地消息表仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final EventOutboxMapper mapper;

    @Override
    public void append(OutboxEvent event) {
        EventOutboxPO po = new EventOutboxPO();
        po.setAggregateType(event.getAggregateType());
        po.setAggregateId(event.getAggregateId());
        po.setEventType(event.getEventType());
        po.setPayload(event.getPayload());
        po.setStatus(OutboxStatus.NEW);
        po.setRetryCount(0);
        po.setCreatedAt(LocalDateTime.now());
        mapper.insert(po);
    }

    @Override
    public List<OutboxEvent> findPending(int limit) {
        LambdaQueryWrapper<EventOutboxPO> wrapper = Wrappers.<EventOutboxPO>lambdaQuery()
                .in(EventOutboxPO::getStatus, OutboxStatus.NEW, OutboxStatus.FAILED)
                .orderByAsc(EventOutboxPO::getCreatedAt)
                .orderByAsc(EventOutboxPO::getId);
        Page<EventOutboxPO> page = mapper.selectPage(new Page<>(1, limit, false), wrapper);
        return page.getRecords().stream().map(this::toDomain).toList();
    }

    @Override
    public void markPublished(Long id) {
        EventOutboxPO po = new EventOutboxPO();
        po.setId(id);
        po.setStatus(OutboxStatus.PUBLISHED);
        po.setPublishedAt(LocalDateTime.now());
        mapper.updateById(po);
    }

    @Override
    public void markFailed(OutboxEvent event) {
        EventOutboxPO po = new EventOutboxPO();
        po.setId(event.getId());
        po.setStatus(event.getStatus());
        po.setRetryCount(event.getRetryCount());
        mapper.updateById(po);
    }

    private OutboxEvent toDomain(EventOutboxPO po) {
        Map<String, Object> payload = po.getPayload() == null ? Map.of() : po.getPayload();
        return new OutboxEvent(po.getId(), po.getAggregateType(), po.getAggregateId(), po.getEventType(),
                payload, po.getStatus(), po.getRetryCount() == null ? 0 : po.getRetryCount(),
                po.getCreatedAt(), po.getPublishedAt());
    }
}
