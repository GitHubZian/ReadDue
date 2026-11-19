package com.zian.backend.notify.infrastructure.mail;

import com.zian.backend.notify.domain.port.MailGateway;
import com.zian.backend.shared.common.Masks;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring Mail 的 SMTP 网关。
 *
 * <p>{@code spring.mail.host} 或账号为空时 {@link #available()} 为 false，消费者据此把投递记为
 * SKIPPED(MAIL_DISABLED)，而不是反复重试。日志中的邮箱账号与收件人一律脱敏。
 */
@Slf4j
@Component
public class SmtpMailGateway implements MailGateway {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String host;
    private final String username;

    public SmtpMailGateway(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@readdue.local}") String from,
            @Value("${spring.mail.host:}") String host,
            @Value("${spring.mail.username:}") String username) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.host = host == null ? "" : host;
        this.username = username == null ? "" : username;
    }

    /** 启动自检：只提示，不阻断启动。 */
    @PostConstruct
    void reportConfiguration() {
        if (!configured()) {
            log.warn("邮件通道未配置，EMAIL 投递将记为 SKIPPED(MAIL_DISABLED)");
            return;
        }
        log.info("邮件通道已配置 host={} account={}", host, Masks.email(username));
    }

    /**
     * 判断邮件通道是否可用：配置了 host/账号且存在 JavaMailSender。
     *
     * @return 可用时为 true
     */
    @Override
    public boolean available() {
        return configured() && mailSenderProvider.getIfAvailable() != null;
    }

    /**
     * 发送纯文本邮件。
     *
     * @param to      收件人
     * @param subject 主题
     * @param body    正文
     */
    @Override
    public void send(String to, String subject, String body) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("邮件服务未配置");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
        log.info("邮件已发送 to={} subject={}", Masks.email(to), Masks.text(subject, 120));
    }

    private boolean configured() {
        return !host.isBlank() && !username.isBlank();
    }
}
