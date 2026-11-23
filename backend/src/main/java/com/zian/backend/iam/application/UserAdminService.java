package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.model.UserStatus;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.domain.repository.UserStatusLogRepository;
import com.zian.backend.iam.interfaces.dto.AuthDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号治理应用服务：拉黑 / 恢复。
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final UserStatusLogRepository statusLogRepository;
    private final AuditRecorder auditRecorder;

    /**
     * 拉黑账号并记录原因与到期时间。
     *
     * @param id      用户 ID
     * @param request 原因与到期时间
     * @return 用户状态
     */
    @Transactional
    public AuthDtos.UserStatusView blacklist(Long id, AuthDtos.BlacklistRequest request) {
        Long operatorId = UserContext.requireId();
        String operatorName = UserContext.require().name();
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号不存在"));
        if (operatorId.equals(id)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "不能拉黑自己");
        }
        UserStatus from = user.getStatus();
        user.blacklist(request.reason(), operatorId, parseExpiry(request.expiresAt()));
        if (userRepository.update(user) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "账号状态已被修改，请刷新后重试");
        }
        statusLogRepository.append(
                id,
                from,
                UserStatus.BLACKLISTED,
                request.reason(),
                user.getBlacklistExpiresAt(),
                operatorId,
                operatorName);
        auditRecorder.record(
                "USER_BLACKLIST",
                "USER",
                id,
                "账号治理",
                Map.of("reason", request.reason(), "expiresAt", String.valueOf(user.getBlacklistExpiresAt())));
        return new AuthDtos.UserStatusView(id, user.getStatus().name(), user.getBlacklistReason());
    }

    /**
     * 解除拉黑。
     *
     * @param id 用户 ID
     * @return 用户状态
     */
    @Transactional
    public AuthDtos.UserStatusView restore(Long id) {
        Long operatorId = UserContext.requireId();
        String operatorName = UserContext.require().name();
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号不存在"));
        if (user.getStatus() == UserStatus.NORMAL) {
            return new AuthDtos.UserStatusView(id, user.getStatus().name(), null);
        }
        user.restore();
        if (userRepository.update(user) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "账号状态已被修改，请刷新后重试");
        }
        statusLogRepository.append(
                id, UserStatus.BLACKLISTED, UserStatus.NORMAL, "管理员解除限制", null, operatorId, operatorName);
        auditRecorder.record("USER_RESTORE", "USER", id, "账号治理", Map.of());
        return new AuthDtos.UserStatusView(id, user.getStatus().name(), null);
    }

    private LocalDateTime parseExpiry(String expiresAt) {
        if (expiresAt == null || expiresAt.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(expiresAt);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "解禁时间格式不正确，应为 yyyy-MM-ddTHH:mm:ss");
        }
    }
}
