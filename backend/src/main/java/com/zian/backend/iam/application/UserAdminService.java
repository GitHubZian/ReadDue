package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.repository.UserRepository;
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
    private final AuditRecorder auditRecorder;

    @Transactional
    public AuthDtos.UserStatusView blacklist(Long id, AuthDtos.BlacklistRequest request) {
        Long operatorId = UserContext.requireId();
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号不存在"));
        if (operatorId.equals(id)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "不能拉黑自己");
        }
        user.blacklist(request.reason(), operatorId, parseExpiry(request.expiresAt()));
        if (userRepository.update(user) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "账号状态已被修改，请刷新后重试");
        }
        auditRecorder.record(
                "USER_BLACKLIST",
                "USER",
                id,
                "账号治理",
                Map.of("reason", request.reason(), "expiresAt", String.valueOf(user.getBlacklistExpiresAt())));
        return new AuthDtos.UserStatusView(id, user.getStatus().name(), user.getBlacklistReason());
    }

    @Transactional
    public AuthDtos.UserStatusView restore(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号不存在"));
        if (user.getStatus() == com.zian.backend.iam.domain.model.UserStatus.NORMAL) {
            return new AuthDtos.UserStatusView(id, user.getStatus().name(), null);
        }
        user.restore();
        if (userRepository.update(user) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "账号状态已被修改，请刷新后重试");
        }
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
