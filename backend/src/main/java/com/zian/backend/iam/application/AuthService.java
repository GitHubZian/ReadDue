package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.model.UserRole;
import com.zian.backend.iam.domain.repository.PermissionRepository;
import com.zian.backend.iam.domain.repository.ReaderSelfRepository;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.interfaces.dto.AuthDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.JwtService;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务：登录 / 当前用户 / 登出。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final ReaderSelfRepository readerSelfRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditRecorder auditRecorder;

    /**
     * 登录：校验密码、记录登录时间、签发令牌并返回权限码。
     *
     * @param request 账号与密码
     * @return 登录结果
     */
    @Transactional
    public AuthDtos.LoginResult login(AuthDtos.LoginRequest request) {
        String userNo = request.userNo().trim();
        User user = userRepository
                .findByUserNo(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误");
        }
        user.touchLogin(LocalDateTime.now());
        userRepository.update(user);

        Set<String> permissions = effectivePermissions(user.getRole());
        LoginUser principal =
                new LoginUser(user.getId(), user.getUserNo(), user.getName(), user.getRole().name(), permissions);
        JwtService.IssuedToken issued = jwtService.issue(principal);
        auditRecorder.recordAs(principal, "AUTH_LOGIN", "USER", user.getId(), "登录系统", Map.of());
        return new AuthDtos.LoginResult(issued.token(), issued.expiresIn(), toView(user, permissions));
    }

    /**
     * 当前会话用户：角色与权限以数据库为准。
     *
     * @return 会话用户
     */
    @Transactional(readOnly = true)
    public AuthDtos.SessionUser me() {
        LoginUser current = UserContext.require();
        User user = userRepository
                .findById(current.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号不存在或已注销"));
        return toView(user, effectivePermissions(user.getRole()));
    }

    /**
     * 注销：吊销令牌对应的 Redis 登录态。
     *
     * @param token 原始令牌
     */
    @Transactional(readOnly = true)
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            jwtService.revoke(token);
        }
    }

    /**
     * 刷新令牌：校验旧令牌、按数据库重算权限、签发新 jti 并吊销旧 jti。
     *
     * <p>旧令牌一经刷新立即失效，避免同一登录态长期有效；响应结构与登录一致。
     *
     * @param token 旧令牌原文（不含 Bearer 前缀）
     * @return 新令牌、有效期与用户信息
     */
    @Transactional
    public AuthDtos.LoginResult refresh(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "缺少令牌，无法刷新");
        }
        LoginUser current = jwtService.parse(token);
        LoginUser fresh = principalOf(current.id());
        JwtService.IssuedToken issued = jwtService.issue(fresh);
        jwtService.revoke(token);
        auditRecorder.recordAs(fresh, "AUTH_REFRESH", "USER", fresh.id(), "刷新令牌", Map.of());
        User user = userRepository
                .findById(fresh.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号不存在或已注销"));
        return new AuthDtos.LoginResult(issued.token(), issued.expiresIn(), toView(user, fresh.permissions()));
    }

    /**
     * 按用户 ID 重建登录主体，供过滤器做权限回源。
     *
     * @param userId 用户 ID
     * @return 登录主体
     */
    @Transactional(readOnly = true)
    public LoginUser principalOf(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号不存在或已注销"));
        return new LoginUser(
                user.getId(),
                user.getUserNo(),
                user.getName(),
                user.getRole().name(),
                effectivePermissions(user.getRole()));
    }
    private Set<String> effectivePermissions(UserRole role) {
        Set<String> codes = new LinkedHashSet<>(permissionRepository.permissionsOf(role));
        if (codes.contains("overdue:board")) {
            codes.add("overdue:read");
        }
        if (codes.contains("user:blacklist")) {
            codes.add("user:manage");
        }
        return codes;
    }

    private AuthDtos.SessionUser toView(User user, Set<String> permissions) {
        ReaderSelfRepository.Profile profile = readerSelfRepository.profile(user.getId());
        return new AuthDtos.SessionUser(
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.getUserNo(),
                null,
                List.copyOf(permissions),
                profile.department(),
                profile.major(),
                profile.grade(),
                readerSelfRepository.totalBorrowed(user.getId()));
    }
}


