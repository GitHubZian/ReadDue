package com.zian.backend.notify.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.zian.backend.notify.application.NotifyService;
import com.zian.backend.shared.constant.MqConstant;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 通知编排消费者：把领域事件翻译成提醒任务。
 *
 * <p>手动 ack：处理失败只记录日志后 ack，避免同一事件无限重投；失败任务本身有状态与重试入口。
 */
@Component
@RequiredArgsConstructor
public class NotifyEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotifyEventListener.class);

    private final ObjectMapper objectMapper;
    private final NotifyService notifyService;

    @RabbitListener(queues = MqConstant.Q_NOTIFY_PLAN)
    public void onNotifyPlan(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        consume(message, channel, tag, "q.notify.plan");
    }

    @RabbitListener(queues = MqConstant.Q_NOTIFY_REPLAN)
    public void onNotifyReplan(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        consume(message, channel, tag, "q.notify.replan");
    }

    private void consume(Message message, Channel channel, long tag, String queue) {
        try {
            handle(message);
            channel.basicAck(tag, false);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            // 消息体本身非法（不可重试）：记录后 ack，避免毒消息无限重投。
            log.error("通知事件格式非法，已丢弃 queue={} err={}", queue, e.getMessage());
            ack(channel, tag);
        } catch (Exception e) {
            // 下游暂时失败（DB/依赖）：重新入队重试，不静默丢失。
            log.error("通知事件处理失败，重新入队 queue={} err={}", queue, e.getMessage(), e);
            try {
                channel.basicNack(tag, false, true);
            } catch (Exception nackError) {
                log.warn("消息 nack 失败: {}", nackError.getMessage());
            }
        }
    }

    private void ack(Channel channel, long tag) {
        try {
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.warn("消息 ack 失败: {}", e.getMessage());
        }
    }

    private void handle(Message message) throws Exception {
        String eventType = String.valueOf(message.getMessageProperties().getHeaders().get("eventType"));
        JsonNode payload = objectMapper.readTree(new String(message.getBody(), StandardCharsets.UTF_8));
        switch (eventType) {
            case MqConstant.EVENT_BORROW_CREATED -> {
                Long orderId = longOf(payload, "orderId");
                Long readerId = longOf(payload, "readerId");
                LocalDate dueAt = LocalDate.parse(payload.path("dueAt").asText());
                if (orderId != null && readerId != null) {
                    notifyService.planReturnReminders(orderId, readerId, dueAt);
                }
            }
            case MqConstant.EVENT_BORROW_RETURNED -> {
                Long orderId = longOf(payload, "orderId");
                if (orderId != null) {
                    notifyService.cancelReminders(orderId);
                }
            }
            case MqConstant.EVENT_OVERDUE_MARKED -> {
                Long orderId = longOf(payload, "orderId");
                Long readerId = longOf(payload, "readerId");
                if (orderId != null && readerId != null) {
                    notifyService.planOverdue(orderId, readerId, payload.path("overdueDays").asInt(1));
                }
            }
            case MqConstant.EVENT_RESERVATION_READY -> {
                Long reservationId = longOf(payload, "reservationId");
                Long readerId = longOf(payload, "readerId");
                Long bookId = longOf(payload, "bookId");
                if (reservationId != null && readerId != null) {
                    notifyService.planReservationReady(
                            reservationId,
                            readerId,
                            bookId,
                            payload.hasNonNull("expiresAt")
                                    ? LocalDateTime.parse(payload.path("expiresAt").asText())
                                    : LocalDateTime.now().plusHours(48));
                }
            }
            case MqConstant.EVENT_GRACE_APPROVED -> {
                Long orderId = longOf(payload, "orderId");
                Long readerId = longOf(payload, "readerId");
                if (orderId != null && readerId != null && payload.hasNonNull("effectiveDueAt")) {
                    notifyService.replanAfterGrace(
                            orderId, readerId, LocalDate.parse(payload.path("effectiveDueAt").asText()));
                }
            }
            default -> log.debug("忽略未订阅的通知事件 {}", eventType);
        }
    }

    private Long longOf(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asLong() : null;
    }
}

