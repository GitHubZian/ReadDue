package com.zian.backend.bootstrap.config;

import com.zian.backend.shared.constant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑（技术架构 §5）。
 *
 * <pre>
 * yyq.event (topic)     borrow.returned → q.reservation.promote
 *                       borrow.returned / borrow.created / overdue.marked → q.notify.plan
 *                       grace.approved → q.notify.replan
 * yyq.delivery (direct) channel.inbox → q.delivery.inbox
 *                       channel.email → q.delivery.email（失败 → q.delivery.dlq）
 * q.delivery.email.retry  TTL 30s 后经 DLX 回到 q.delivery.email（延迟可用 TTL+DLX 实现，不用插件）
 * </pre>
 */
@Configuration
public class RabbitTopologyConfig {

    public static final int EMAIL_RETRY_TTL_MS = 30_000;

    @Bean
    public Declarables rabbitTopology() {
        TopicExchange eventExchange = new TopicExchange(MqConstant.EVENT_EXCHANGE, true, false);
        DirectExchange deliveryExchange = new DirectExchange(MqConstant.DELIVERY_EXCHANGE, true, false);

        Queue reservationPromote = QueueBuilder.durable(MqConstant.Q_RESERVATION_PROMOTE).build();
        Queue notifyPlan = QueueBuilder.durable(MqConstant.Q_NOTIFY_PLAN).build();
        Queue notifyReplan = QueueBuilder.durable(MqConstant.Q_NOTIFY_REPLAN).build();
        Queue deliveryInbox = QueueBuilder.durable(MqConstant.Q_DELIVERY_INBOX).build();
        Queue deliveryEmail = QueueBuilder.durable(MqConstant.Q_DELIVERY_EMAIL)
                .deadLetterExchange(MqConstant.DELIVERY_EXCHANGE)
                .deadLetterRoutingKey(MqConstant.RK_DELIVERY_DEAD)
                .build();
        Queue deliveryEmailRetry = QueueBuilder.durable(MqConstant.Q_DELIVERY_EMAIL_RETRY)
                .ttl(EMAIL_RETRY_TTL_MS)
                .deadLetterExchange(MqConstant.DELIVERY_EXCHANGE)
                .deadLetterRoutingKey(MqConstant.RK_CHANNEL_EMAIL)
                .build();
        Queue deliveryDlq = QueueBuilder.durable(MqConstant.Q_DELIVERY_DLQ).build();

        return new Declarables(
                eventExchange, deliveryExchange,
                reservationPromote, notifyPlan, notifyReplan,
                deliveryInbox, deliveryEmail, deliveryEmailRetry, deliveryDlq,
                // 预约兑现：只在归还事件上触发
                BindingBuilder.bind(reservationPromote).to(eventExchange).with(MqConstant.RK_BORROW_RETURNED),
                // 提醒编排：借出、归还、逾期升级都要重排
                BindingBuilder.bind(notifyPlan).to(eventExchange).with(MqConstant.RK_BORROW_CREATED),
                BindingBuilder.bind(notifyPlan).to(eventExchange).with(MqConstant.RK_BORROW_RETURNED),
                BindingBuilder.bind(notifyPlan).to(eventExchange).with(MqConstant.RK_OVERDUE_MARKED),
                BindingBuilder.bind(notifyPlan).to(eventExchange).with(MqConstant.RK_RESERVATION_READY),
                // 宽限通过后重排提醒（有效应还日变化）
                BindingBuilder.bind(notifyReplan).to(eventExchange).with(MqConstant.RK_GRACE_APPROVED),
                BindingBuilder.bind(deliveryInbox).to(deliveryExchange).with(MqConstant.RK_CHANNEL_INBOX),
                BindingBuilder.bind(deliveryEmail).to(deliveryExchange).with(MqConstant.RK_CHANNEL_EMAIL),
                BindingBuilder.bind(deliveryDlq).to(deliveryExchange).with(MqConstant.RK_DELIVERY_DEAD));
    }

    /**
     * 编译期保证绑定对象未被优化掉（Declarables 已声明全部拓扑，此方法仅作可读性锚点）。
     */
    static Binding binding(Queue queue, DirectExchange exchange, String key) {
        return BindingBuilder.bind(queue).to(exchange).with(key);
    }
}
