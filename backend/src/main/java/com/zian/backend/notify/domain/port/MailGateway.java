package com.zian.backend.notify.domain.port;

/**
 * 邮件发送网关（SMTP 由基础设施实现；未配置 SMTP 时 {@link #available()} 返回 false）。
 */
public interface MailGateway {

    boolean available();

    void send(String to, String subject, String body);
}
