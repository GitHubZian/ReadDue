package com.zian.backend.shared.constant;

/**
 * RabbitMQ 拓扑常量（技术架构 §5）。
 *
 * <p>延迟一律用 TTL + DLX 实现，不依赖延迟插件。
 */
public interface MqConstant {

    /** 业务事件交换机（topic） */
    String EVENT_EXCHANGE = "yyq.event";

    /** 渠道投递交换机（direct） */
    String DELIVERY_EXCHANGE = "yyq.delivery";

    /** 投递死信交换机 */
    String DELIVERY_DLX = "yyq.delivery.dlx";

    // ---------- routing key ----------
    String RK_BORROW_RETURNED = "borrow.returned";
    String RK_BORROW_CREATED = "borrow.created";
    String RK_GRACE_APPROVED = "grace.approved";
    String RK_OVERDUE_MARKED = "overdue.marked";
    String RK_RESERVATION_READY = "reservation.ready";
    String RK_CHANNEL_EMAIL = "channel.email";
    String RK_CHANNEL_INBOX = "channel.inbox";
    String RK_DELIVERY_DEAD = "delivery.dead";

    // ---------- queue ----------
    String Q_RESERVATION_PROMOTE = "q.reservation.promote";
    String Q_NOTIFY_PLAN = "q.notify.plan";
    String Q_NOTIFY_REPLAN = "q.notify.replan";
    String Q_DELIVERY_EMAIL = "q.delivery.email";
    String Q_DELIVERY_INBOX = "q.delivery.inbox";
    String Q_DELIVERY_DLQ = "q.delivery.dlq";

    /** 邮件重试队列（TTL 30s 后回到 q.delivery.email） */
    String Q_DELIVERY_EMAIL_RETRY = "q.delivery.email.retry";

    // ---------- event type ----------
    String EVENT_BORROW_CREATED = "BORROW_CREATED";
    String EVENT_BORROW_RETURNED = "BORROW_RETURNED";
    String EVENT_GRACE_APPROVED = "GRACE_APPROVED";
    String EVENT_OVERDUE_MARKED = "OVERDUE_MARKED";
    String EVENT_APPEAL_SLA_BREACHED = "APPEAL_SLA_BREACHED";
    String EVENT_RESERVATION_READY = "RESERVATION_READY";
}
