package com.zian.backend.shared.security;

import cn.hutool.core.util.IdUtil;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * JWT 签发 / 校验。
 *
 * <p>无状态校验 + 有状态吊销：token 内带 {@code jti}，签发时在 Redis 写入
 * {@code readdue:auth:token:{jti}}（2h，与 token 同寿命）；登出即删除该键，于是旧 token 立即失效。
 * 这样既避免每请求查库，又支持真正的「退出登录」。
 */
@Service
public class JwtService {

    private static final String KEY_PREFIX = "readdue:auth:token:";
    private static final String DEV_SECRET = "readdue-dev-secret-key-please-change-in-production-2026";
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final String issuer;
    private final long ttlSeconds;
    private final StringRedisTemplate redis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer:readdue}") String issuer,
                      @Value("${app.jwt.ttl-seconds:7200}") long ttlSeconds,
                      StringRedisTemplate redis,
                      Environment environment) {
        if (DEV_SECRET.equals(secret)) {
            if (!Arrays.asList(environment.getActiveProfiles()).contains("local")) {
                throw new IllegalStateException(
                        "APP_JWT_SECRET 未设置：非 local 环境禁止使用内置默认密钥，请在环境变量中提供 ≥32 字节随机密钥");
            }
            log.warn("正在使用内置默认 JWT 密钥，仅限本地演示；部署前必须设置 APP_JWT_SECRET");
        }
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("APP_JWT_SECRET 长度不足 32 字节，HMAC-SHA256 需要更长密钥");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
        this.redis = redis;
    }

    /**
     * 令牌有效期（秒）。
     *
     * @return 有效期
     */
    public long ttlSeconds() {
        return ttlSeconds;
    }

    /**
     * 签发令牌，并在 Redis 记录 jti → 用户 ID 的登录态。
     *
     * @param user 登录主体
     * @return 令牌与有效期
     */
    public IssuedToken issue(LoginUser user) {
        String jti = IdUtil.fastSimpleUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        String token = Jwts.builder()
                .subject(String.valueOf(user.id()))
                .id(jti)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("userNo", user.userNo())
                .claim("name", user.name())
                .claim("role", user.role())
                .claim("perms", new ArrayList<>(user.permissions()))
                .signWith(key)
                .compact();
        redis.opsForValue().set(KEY_PREFIX + jti, String.valueOf(user.id()), Duration.ofSeconds(ttlSeconds));
        return new IssuedToken(token, ttlSeconds);
    }

    /**
     * 解析并校验令牌：签名、签发方与 Redis 登录态缺一不可。
     *
     * @param token 原始令牌
     * @return 登录主体
     */
    @SuppressWarnings("unchecked")
    public LoginUser parse(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String jti = claims.getId();
        String subject = claims.getSubject();
        String bound = jti == null ? null : redis.opsForValue().get(KEY_PREFIX + jti);
        // 必须与签发时登记的用户一致：复用他人 jti 或伪造 sub 都会被拒。
        if (jti == null || bound == null || !bound.equals(subject)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录态已失效，请重新登录");
        }
        String role = claims.get("role", String.class);
        Object rawPerms = claims.get("perms");
        List<String> perms = rawPerms instanceof List<?> list
                ? (List<String>) list
                : List.of();
        return new LoginUser(
                Long.valueOf(subject),
                claims.get("userNo", String.class),
                claims.get("name", String.class),
                role == null ? "READER" : role,
                new LinkedHashSet<>(perms));
    }

    /**
     * 吊销令牌：删除对应的 Redis 登录态。
     *
     * @param token 原始令牌
     */
    public void revoke(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getId() != null) {
                redis.delete(KEY_PREFIX + claims.getId());
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // token 本身已不可用，登出视为成功
        }
    }

    /**
     * 签发结果。
     *
     * @param token     JWT 字符串
     * @param expiresIn 有效秒数
     */
    public record IssuedToken(String token, long expiresIn) {
    }
}

