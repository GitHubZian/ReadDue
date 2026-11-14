package com.zian.backend.shared.outbox.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

/**
 * 本地消息表事件（transactional outbox）。
 *
 * <p>业务事务内只落库，事务提交后由 {@code OutboxPublisher} 异步投递 RabbitMQ，
 * 从而保证「业务提交」与「消息发出」最终一致；重复投递由消费端按业务主键幂等消化。
 */
@Getter
public class OutboxEvent {

    private final Long id;
    private final String aggregateType;
    private final Long aggregateId;
    private final String eventType;
    private final Map<String, Object> payload;
    private String status;
    private int retryCount;
    private final LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public OutboxEvent(Long id, String aggregateType, Long aggregateId, String eventType,
                       Map<String, Object> payload, String status, int retryCount,
                       LocalDateTime createdAt, LocalDateTime publishedAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload == null ? Map.of() : Map.copyOf(payload);
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    /**
     * 构造一条待投递事件。
     */
    public static OutboxEvent pending(String aggregateType, Long aggregateId, String eventType,
                                      Map<String, Object> payload) {
        return new OutboxEvent(null, aggregateType, aggregateId, eventType, payload,
                OutboxStatus.NEW, 0, LocalDateTime.now(), null);
    }

    /**
     * 标记已投递。
     */
    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    /**
     * 记录失败并按上限判定是否转 DEAD。
     */
    public boolean markFailed(int maxRetry) {
        this.retryCount++;
        if (this.retryCount >= maxRetry) {
            this.status = OutboxStatus.DEAD;
            return true;
        }
        this.status = OutboxStatus.FAILED;
        return false;
    }

    /**
     * 把失败事件重置为待投递，供人工重放。
     */
    public void resetToNew() {
        this.status = OutboxStatus.NEW;
    }
}
