package com.zian.backend.notify.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zian.backend.iam.application.UserQueryService;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.model.UserRole;
import com.zian.backend.iam.domain.model.UserStatus;
import com.zian.backend.notify.domain.port.MailGateway;
import com.zian.backend.notify.domain.repository.NotifyDeliveryRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * 邮件投递落库分支：SENT / SKIPPED(NO_EMAIL) / SKIPPED(MAIL_DISABLED) / FAILED(SEND_FAILED)。
 */
class EmailDeliveryServiceTest {

    private static final long TASK_ID = 10L;
    private static final long READER_ID = 1L;

    private final UserQueryService userQueryService = mock(UserQueryService.class);
    private final MailGateway mailGateway = mock(MailGateway.class);
    private final NotifyDeliveryRepository deliveryRepository = mock(NotifyDeliveryRepository.class);
    private final EmailDeliveryService service =
            new EmailDeliveryService(userQueryService, mailGateway, deliveryRepository);

    @Test
    void recordsSkippedWhenMailChannelDisabled() {
        when(userQueryService.requireById(READER_ID)).thenReturn(reader("chen@example.com"));
        when(mailGateway.available()).thenReturn(false);

        service.deliver(TASK_ID, READER_ID, "图书逾期提醒", "请尽快归还");

        verify(mailGateway, never()).send(anyString(), anyString(), anyString());
        verify(deliveryRepository).append(TASK_ID, "EMAIL", "SKIPPED", 1, "MAIL_DISABLED", "未配置 SMTP");
    }

    @Test
    void recordsSkippedWhenReaderHasNoEmail() {
        when(userQueryService.requireById(READER_ID)).thenReturn(reader("  "));

        service.deliver(TASK_ID, READER_ID, "图书逾期提醒", "请尽快归还");

        verify(mailGateway, never()).available();
        verify(mailGateway, never()).send(anyString(), anyString(), anyString());
        verify(deliveryRepository).append(TASK_ID, "EMAIL", "SKIPPED", 1, "NO_EMAIL", "读者未填写邮箱");
    }

    @Test
    void recordsSentWhenSendSucceeds() {
        when(userQueryService.requireById(READER_ID)).thenReturn(reader("chen@example.com"));
        when(mailGateway.available()).thenReturn(true);
        when(mailGateway.send("chen@example.com", "图书逾期提醒", "请尽快归还"))
                .thenReturn("<msg-1@qq.com>");

        service.deliver(TASK_ID, READER_ID, "图书逾期提醒", "请尽快归还");

        verify(mailGateway).send("chen@example.com", "图书逾期提醒", "请尽快归还");
        verify(deliveryRepository).append(TASK_ID, "EMAIL", "SENT", 1, null, null, "<msg-1@qq.com>");
    }

    @Test
    void recordsFailureAndRethrowsSoListenerCanRetry() {
        when(userQueryService.requireById(READER_ID)).thenReturn(reader("chen@example.com"));
        when(mailGateway.available()).thenReturn(true);
        doThrow(new IllegalStateException("454 Authentication failed"))
                .when(mailGateway)
                .send(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> service.deliver(TASK_ID, READER_ID, "图书逾期提醒", "请尽快归还"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authentication failed");
        verify(deliveryRepository).append(TASK_ID, "EMAIL", "FAILED", 1, "SEND_FAILED", "454 Authentication failed", null);
    }

    private static User reader(String email) {
        return new User(
                READER_ID, "2021001", "陈昊", UserRole.READER, null, email, "x",
                UserStatus.NORMAL, null, null, null, null, null, 0, LocalDateTime.now());
    }
}
