package com.zian.backend.notify.domain.port;

/**
 * 邮件投递入队口：应用层只投递意图，真正发送由 {@code q.delivery.email} 消费者完成。
 */
public interface EmailNotifier {

    void enqueue(Long taskId, Long readerId, String subject, String body);
}
