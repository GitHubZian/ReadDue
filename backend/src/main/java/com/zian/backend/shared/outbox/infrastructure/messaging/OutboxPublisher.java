package com.zian.backend.shared.outbox.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.outbox.domain.model.OutboxEvent;
import com.zian.backend.shared.outbox.domain.repository.OutboxRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 本地消息表投递器：定时扫描 NEW / FAILED 事件并发送到 {@code yyq.event}。
 *
 * <p>「先落库、后投递」，投递失败只累加重试次数，超过上限进入 DEAD 等待人工重放；
 * 消费端按业务主键幂等，因此允许至少一次投递带来的重复消息。
 */
@Component
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private final int batchSize;
    private final int maxRetry;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper,
                           @Value("${app.outbox.batch-size:50}") int batchSize,
                           @Value("${app.outbox.max-retry:5}") int maxRetry) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.maxRetry = maxRetry;
    }

    /**
     * 定时扫描待投递事件并发送到 RabbitMQ，成功标记 PUBLISHED，失败按次数重试或置 DEAD。
     */
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void publishPending() {
        List<OutboxEvent> pending;
        try {
            pending = outboxRepository.findPending(batchSize);
        } catch (Exception e) {
            log.warn("扫描 event_outbox 失败: {}", e.getMessage());
            return;
        }
        if (pending.isEmpty()) {
            return;
        }
        for (OutboxEvent event : pending) {
            try {
                rabbitTemplate.send(MqConstant.EVENT_EXCHANGE, routingKey(event.getEventType()), toMessage(event));
                outboxRepository.markPublished(event.getId());
            } catch (Exception e) {
                boolean dead = event.markFailed(maxRetry);
                outboxRepository.markFailed(event);
                if (dead) {
                    log.error("outbox 事件投递失败已进入 DEAD: id={}, type={}, err={}",
                            event.getId(), event.getEventType(), e.getMessage());
                } else {
                    log.debug("outbox 事件投递失败，等待重试: id={}, retry={}, err={}",
                            event.getId(), event.getRetryCount(), e.getMessage());
                }
            }
        }
    }

    private Message toMessage(OutboxEvent event) throws Exception {
        byte[] body = objectMapper.writeValueAsBytes(event.getPayload());
        return org.springframework.amqp.core.MessageBuilder.withBody(body)
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setMessageId(String.valueOf(event.getId()))
                .setHeader("eventType", event.getEventType())
                .setHeader("aggregateType", event.getAggregateType())
                .setHeader("aggregateId", event.getAggregateId())
                .build();
    }

    /**
     * 事件类型 → routing key。未知事件按「大写转点分小写」兜底，保证不被静默丢弃。
     */
    static String routingKey(String eventType) {
        return switch (eventType) {
            case MqConstant.EVENT_BORROW_RETURNED -> MqConstant.RK_BORROW_RETURNED;
            case MqConstant.EVENT_BORROW_CREATED -> MqConstant.RK_BORROW_CREATED;
            case MqConstant.EVENT_BORROW_RENEWED -> MqConstant.RK_BORROW_RENEWED;
            case MqConstant.EVENT_GRACE_APPROVED -> MqConstant.RK_GRACE_APPROVED;
            case MqConstant.EVENT_OVERDUE_MARKED -> MqConstant.RK_OVERDUE_MARKED;
            case MqConstant.EVENT_RESERVATION_READY -> MqConstant.RK_RESERVATION_READY;
            default -> eventType == null ? "unknown" : eventType.toLowerCase().replace('_', '.');
        };
    }
}
