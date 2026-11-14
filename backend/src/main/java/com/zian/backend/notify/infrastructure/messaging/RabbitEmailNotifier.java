package com.zian.backend.notify.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.notify.domain.port.EmailNotifier;
import com.zian.backend.shared.constant.MqConstant;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 把邮件意图投递到 {@code yyq.delivery / channel.email}，由消费者异步发送。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEmailNotifier implements EmailNotifier {

    /** 首次投递，重试次数由消费者累加。 */
    private static final int FIRST_ATTEMPT = 0;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 把邮件投递任务发到投递交换机，由消费者异步发送。
     *
     * @param taskId   通知任务 ID
     * @param readerId 读者 ID
     * @param subject  主题
     * @param body     正文
     */
    @Override
    public void enqueue(Long taskId, Long readerId, String subject, String body) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("readerId", readerId);
        payload.put("subject", subject);
        payload.put("body", body);
        try {
            Message message =
                    MessageBuilder.withBody(objectMapper.writeValueAsBytes(payload))
                            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                            .setContentEncoding(StandardCharsets.UTF_8.name())
                            .setHeader("attempt", FIRST_ATTEMPT)
                            .build();
            rabbitTemplate.send(MqConstant.DELIVERY_EXCHANGE, MqConstant.RK_CHANNEL_EMAIL, message);
        } catch (Exception e) {
            log.warn("邮件消息投递失败 taskId={}: {}", taskId, e.getMessage());
        }
    }
}
