package com.zian.backend.notify.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zian.backend.shared.common.Masks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 网关可用性判定：host 与账号任一为空、或容器没有 JavaMailSender 时，一律视为未配置。
 */
class SmtpMailGatewayTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);

    @Test
    void unavailableWhenHostMissing() {
        when(provider.getIfAvailable()).thenReturn(new JavaMailSenderImpl());
        SmtpMailGateway gateway = new SmtpMailGateway(provider, "a@qq.com", "", "a@qq.com");

        assertThat(gateway.available()).isFalse();
    }

    @Test
    void unavailableWhenAccountMissing() {
        when(provider.getIfAvailable()).thenReturn(new JavaMailSenderImpl());
        SmtpMailGateway gateway = new SmtpMailGateway(provider, "no-reply@readdue.local", "smtp.qq.com", "");

        assertThat(gateway.available()).isFalse();
    }

    @Test
    void unavailableWhenSenderBeanAbsent() {
        when(provider.getIfAvailable()).thenReturn(null);
        SmtpMailGateway gateway = new SmtpMailGateway(provider, "a@qq.com", "smtp.qq.com", "a@qq.com");

        assertThat(gateway.available()).isFalse();
        assertThatThrownBy(() -> gateway.send("b@qq.com", "s", "b"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("邮件服务未配置");
    }

    @Test
    void masksAccountInLogs() {
        assertThat(Masks.email("2927251542@qq.com")).isEqualTo("2***@qq.com");
        assertThat(Masks.email(null)).isEqualTo("***");
    }
}
