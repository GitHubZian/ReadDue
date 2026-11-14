package com.zian.backend.notify.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 真实 SMTP 连通性测试：只有本机配置了 MAIL_PASSWORD 才执行，且只发给 MAIL_USERNAME 自己。
 *
 * <p>运行：<code>mvn -q test -Dtest=SmtpMailGatewayIntegrationTest</code>
 */
@EnabledIfEnvironmentVariable(named = "MAIL_PASSWORD", matches = ".+")
class SmtpMailGatewayIntegrationTest {

    @Test
    void sendsRealMailToOwnAccount() {
        String account = System.getenv("MAIL_USERNAME");
        assertThat(account).as("MAIL_USERNAME 必须与 MAIL_PASSWORD 一起配置").isNotBlank();

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.qq.com");
        sender.setPort(465);
        sender.setUsername(account);
        sender.setPassword(System.getenv("MAIL_PASSWORD"));
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        SmtpMailGateway gateway = new SmtpMailGateway(providerOf(sender), account, "smtp.qq.com", account);

        assertThat(gateway.available()).isTrue();
        gateway.send(account, "[阅有期] SMTP 连通性自测", "这是一封由集成测试发出的自测邮件，收到即表示 SMTP 配置可用。");
    }

    private static ObjectProvider<JavaMailSender> providerOf(JavaMailSender sender) {
        return new ObjectProvider<JavaMailSender>() {
            @Override
            public JavaMailSender getObject() {
                return sender;
            }

            @Override
            public JavaMailSender getObject(Object... args) {
                return sender;
            }

            @Override
            public JavaMailSender getIfAvailable() {
                return sender;
            }

            @Override
            public JavaMailSender getIfUnique() {
                return sender;
            }
        };
    }
}
