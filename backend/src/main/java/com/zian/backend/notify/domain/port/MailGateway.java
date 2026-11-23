package com.zian.backend.notify.domain.port;

/**
 * 邮件发送网关（SMTP 由基础设施实现；未配置 SMTP 时 {@link #available()} 返回 false）。
 */
public interface MailGateway {

    boolean available();

    /**
     * 发送纯文本邮件。
     *
     * @return 本次邮件的 Message-ID（用于投递记录溯源），无法获取时为 null
     */
    String send(String to, String subject, String body);
}
