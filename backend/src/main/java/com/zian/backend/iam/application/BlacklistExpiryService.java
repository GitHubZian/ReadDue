package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.model.UserStatus;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.domain.repository.UserStatusLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 拉黑到期扫描：把 {@code blacklist_expires_at} 已过的账号恢复为 NORMAL，
 * 并写 {@code user_status_log} 与审计（{@code USER_UNBLOCK_EXPIRE}，来源系统）。
 *
 * <p>查询侧的 {@link User#isBlacklisted()} 也会把已到期视为 NORMAL，
 * 因此扫描间隔只影响状态回写与留痕，不影响功能拦截。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistExpiryService {

    private static final int BATCH = 100;

    private final UserRepository userRepository;
    private final UserStatusLogRepository statusLogRepository;
    private final AuditRecorder auditRecorder;

    /**
     * 扫描并恢复已到期的拉黑账号。
     */
    @Scheduled(fixedDelayString = "${app.user.blacklist-scan-ms:60000}", initialDelay = 30_000)
    @Transactional
    public void restoreExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<User> expired = userRepository.findExpiredBlacklist(now, BATCH);
        for (User user : expired) {
            LocalDateTime expiresAt = user.getBlacklistExpiresAt();
            user.restore();
            if (userRepository.update(user) == 0) {
                continue;
            }
            statusLogRepository.append(
                    user.getId(),
                    UserStatus.BLACKLISTED,
                    UserStatus.NORMAL,
                    "到期自动解除",
                    expiresAt,
                    null,
                    "系统");
            auditRecorder.record(
                    "USER_UNBLOCK_EXPIRE",
                    "USER",
                    user.getId(),
                    "账号治理",
                    Map.of("reason", "到期自动解除", "expiresAt", String.valueOf(expiresAt)));
        }
        if (!expired.isEmpty()) {
            log.info("拉黑到期恢复 {} 个账号", expired.size());
        }
    }
}
