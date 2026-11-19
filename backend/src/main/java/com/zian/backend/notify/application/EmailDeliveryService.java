package com.zian.backend.notify.application;

import com.zian.backend.iam.application.UserQueryService;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.notify.domain.port.MailGateway;
import com.zian.backend.notify.domain.repository.NotifyDeliveryRepository;
import com.zian.backend.shared.common.Masks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邮件投递执行：查邮箱 → 发信 → 落投递记录。
 *
 * <p>不包事务：失败记录必须落库，异常抛给监听器决定重试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryService {

    private final UserQueryService userQueryService;
    private final MailGateway mailGateway;
    private final NotifyDeliveryRepository deliveryRepository;

    /**
     * 执行一次邮件投递并落投递记录：未配置/无邮箱记 SKIPPED，失败记 FAILED 后重抛。
     *
     * @param taskId   通知任务 ID
     * @param readerId 读者 ID
     * @param subject  主题
     * @param body     正文
     */
    public void deliver(Long taskId, Long readerId, String subject, String body) {
        User reader = userQueryService.requireById(readerId);
        String email = reader.getEmail();
        if (email == null || email.isBlank()) {
            deliveryRepository.append(taskId, "EMAIL", "SKIPPED", 1, "NO_EMAIL", "读者未填写邮箱");
            return;
        }
        if (!mailGateway.available()) {
            deliveryRepository.append(taskId, "EMAIL", "SKIPPED", 1, "MAIL_DISABLED", "未配置 SMTP");
            return;
        }
        try {
            mailGateway.send(email, subject, body);
            deliveryRepository.append(taskId, "EMAIL", "SENT", 1, null, null);
        } catch (RuntimeException e) {
            String reason = Masks.text(e.getMessage(), 200);
            log.warn("邮件发送失败 taskId={} to={}: {}", taskId, Masks.email(email), reason);
            deliveryRepository.append(taskId, "EMAIL", "FAILED", 1, "SEND_FAILED", reason);
            throw e;
        }
    }
}
