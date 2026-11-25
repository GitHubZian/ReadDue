package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.model.UserRole;
import com.zian.backend.iam.domain.model.UserStatus;
import com.zian.backend.iam.domain.repository.PermissionRepository;
import com.zian.backend.iam.domain.repository.ReaderSelfRepository;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.interfaces.dto.AuthDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.JwtService;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    private static final String LOGIN_FAIL_PREFIX = "readdue:auth:fail:";
    private static final String LOGIN_LOCK_PREFIX = "readdue:auth:lock:";
    private static final int FAIL_WINDOW_SECONDS = 60;
    private static final int LOCK_SECONDS = 900;
    private static final int MINUTE_FAIL_LIMIT = 5;
    private static final int LOCK_FAIL_THRESHOLD = 10;

    /**
     * 登录：校验密码、记录登录时间、签发令牌并返回权限码。
     *
     * @param request 账号与密码
     * @param clientIp 客户端 IP（用于失败限流）
     * @return 登录结果
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public AuthDtos.LoginResult login(AuthDtos.LoginRequest request, String clientIp) {
        String userNo = request.userNo().trim();
        String ip = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp;
        if (locked(ip, userNo)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "尝试过于频繁，请稍后再试");
        }
        User user = userRepository.findByUserNo(userNo).orElse(null);
        if (user == null) {
            recordFailure(0, userNo, ip);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号已注销");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordFailure(user.getId(), userNo, ip);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误");
        }
        clearFailures(ip, userNo);
        user.touchLogin(LocalDateTime.now());
        userRepository.update(user);

        Set<String> permissions = effectivePermissions(user.getRole());
        LoginUser principal =
                new LoginUser(user.getId(), user.getUserNo(), user.getName(), user.getRole().name(), permissions);
        JwtService.IssuedToken issued = jwtService.issue(principal);
        auditRecorder.recordAs(principal, "AUTH_LOGIN", "USER", user.getId(), "登录系统", Map.of());
        return new AuthDtos.LoginResult(issued.token(), issued.expiresIn(), toView(user, permissions));
    }

    private boolean locked(String ip, String userNo) {
        return count(LOGIN_LOCK_PREFIX + "ip:" + ip) >= LOCK_FAIL_THRESHOLD
                || count(LOGIN_LOCK_PREFIX + "user:" + userNo) >= LOCK_FAIL_THRESHOLD;
    }

    /** 失败计数 + 审计；达到阈值时抛出 429（审计先落库，事务不因异常回滚）。 */
    private void recordFailure(long userId, String userNo, String ip) {
        long ipMinute = increment(LOGIN_FAIL_PREFIX + "ip:" + ip, Duration.ofSeconds(FAIL_WINDOW_SECONDS));
        long userMinute = increment(LOGIN_FAIL_PREFIX + "user:" + userNo, Duration.ofSeconds(FAIL_WINDOW_SECONDS));
        long ipLock = increment(LOGIN_LOCK_PREFIX + "ip:" + ip, Duration.ofSeconds(LOCK_SECONDS));
        long userLock = increment(LOGIN_LOCK_PREFIX + "user:" + userNo, Duration.ofSeconds(LOCK_SECONDS));
        auditRecorder.record(
                "AUTH_LOGIN_FAIL",
                "USER",
                userId,
                "登录失败",
                Map.of("userNo", userNo, "ip", ip));
        if (ipMinute > MINUTE_FAIL_LIMIT
                || userMinute > MINUTE_FAIL_LIMIT
                || ipLock >= LOCK_FAIL_THRESHOLD
                || userLock >= LOCK_FAIL_THRESHOLD) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "尝试过于频繁，请稍后再试");
        }
    }

    private long increment(String key, Duration ttl) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, ttl);
        }
        return value == null ? 0 : value;
    }

    private long count(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void clearFailures(String ip, String userNo) {
        redisTemplate.delete(List.of(
                LOGIN_FAIL_PREFIX + "ip:" + ip,
                LOGIN_FAIL_PREFIX + "user:" + userNo,
                LOGIN_LOCK_PREFIX + "ip:" + ip,
                LOGIN_LOCK_PREFIX + "user:" + userNo));
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
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号已注销");
        }
        return toView(user, effectivePermissions(user.getRole()));
    }

    /**
     * 读者自助修改资料（姓名/手机/邮箱），返回与 {@code GET /me} 相同的会话结构。
     *
     * <p>字段为 null 表示不修改；手机/邮箱传空串表示清空。失焦的字段（未传）保持原值。
     *
     * @param request 修改内容
     * @return 更新后的会话用户
     */
    @Transactional
    public AuthDtos.SessionUser updateMe(AuthDtos.UpdateProfileRequest request) {
        LoginUser current = UserContext.require();
        User user = userRepository
                .findById(current.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号不存在或已注销"));
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号已注销");
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少修改内容");
        }
        String name = request.name() == null ? user.getName() : request.name().trim();
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "姓名不能为空");
        }
        if (name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "姓名长度为 1-20 个字符");
        }
        String phone = request.phone() == null ? user.getPhone() : request.phone().trim();
        if (phone != null && phone.isEmpty()) {
            phone = null;
        }
        if (phone != null && !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号需为 11 位数字且以 1 开头");
        }
        String email = request.email() == null ? user.getEmail() : request.email().trim();
        if (email != null && email.isEmpty()) {
            email = null;
        }
        if (email != null && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        if (email != null && email.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱过长");
        }
        userRepository.updateProfile(user.getId(), name, phone, email);
        auditRecorder.record(
                "USER_PROFILE_UPDATE",
                "SYS_USER",
                user.getId(),
                "读者更新联系方式",
                Map.of(
                        "hasPhone", phone != null,
                        "hasEmail", email != null,
                        "changedName", !name.equals(user.getName())));
        return me();
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
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号已注销");
        }
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
                readerSelfRepository.totalBorrowed(user.getId()),
                user.getPhone(),
                user.getEmail());
    }
}


