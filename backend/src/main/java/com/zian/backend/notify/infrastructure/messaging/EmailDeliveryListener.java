package com.zian.backend.notify.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.zian.backend.notify.application.EmailDeliveryService;
import com.zian.backend.shared.constant.MqConstant;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 邮件投递消费者：失败先进 30s 重试队列，超过 2 次重试后进死信队列，不静默丢弃。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailDeliveryListener {

    private static final int MAX_ATTEMPT = 2;

    private final ObjectMapper objectMapper;
    private final EmailDeliveryService deliveryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = MqConstant.Q_DELIVERY_EMAIL)
    public void consume(
            Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        int attempt = readAttempt(message);
        try {
            JsonNode node = objectMapper.readTree(message.getBody());
            deliveryService.deliver(
                    node.path("taskId").asLong(),
                    node.path("readerId").asLong(),
                    node.path("subject").asText("阅有期通知"),
                    node.path("body").asText(""));
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.warn("邮件投递失败，第 {} 次：{}", attempt + 1, e.getMessage());
            requeueOrDeadLetter(message, attempt);
            channel.basicAck(tag, false);
        }
    }

    private void requeueOrDeadLetter(Message message, int attempt) {
        boolean dead = attempt >= MAX_ATTEMPT;
        String target =
                dead
                        ? MqConstant.RK_DELIVERY_DEAD
                        : MqConstant.Q_DELIVERY_EMAIL_RETRY;
        String exchange = dead ? MqConstant.DELIVERY_EXCHANGE : "";
        Message next =
                MessageBuilder.withBody(message.getBody())
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .setContentEncoding(StandardCharsets.UTF_8.name())
                        .setHeader("attempt", attempt + 1)
                        .build();
        rabbitTemplate.send(exchange, target, next);
        if (dead) {
            log.error("邮件投递进入死信队列，请人工排查");
        }
    }

    private static int readAttempt(Message message) {
        Object raw = message.getMessageProperties().getHeader("attempt");
        return raw instanceof Number number ? number.intValue() : 0;
    }
}
