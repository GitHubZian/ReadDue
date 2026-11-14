package com.zian.backend.shared.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.ResultUtils;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * 写接口幂等：同一登录用户携带同一个 {@code Idempotency-Key} 重复提交时，直接回放首次响应。
 *
 * <p>只缓存已认证用户的 2xx 响应，保留 24 小时；匿名请求（登录）不缓存，避免不同用户复用同一个 key
 * 互相看到对方的令牌。缓存键带用户维度，跨用户不会命中。
 */
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER = "Idempotency-Key";

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String KEY_PREFIX = "readdue:idem:";
    private static final Duration TTL = Duration.ofHours(24);
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final int MAX_KEY_LENGTH = 128;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String rawKey = request.getHeader(HEADER);
        LoginUser user = UserContext.current();
        if (!WRITE_METHODS.contains(request.getMethod()) || rawKey == null || rawKey.isBlank() || user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = rawKey.trim();
        if (key.length() > MAX_KEY_LENGTH) {
            writeJson(response, ErrorCode.PARAMS_ERROR.getHttpStatus(),
                    ResultUtils.error(ErrorCode.PARAMS_ERROR, "Idempotency-Key 长度不能超过 128"));
            return;
        }

        String cacheKey = KEY_PREFIX + user.id() + ":" + key;
        CachedResponse cached = read(cacheKey);
        if (cached != null) {
            writeJson(response, cached.status(), cached.body());
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
            int status = wrapper.getStatus();
            if (status >= 200 && status < 300) {
                String body = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                write(cacheKey, new CachedResponse(status, body));
            }
        } finally {
            wrapper.copyBodyToResponse();
        }
    }

    private CachedResponse read(String cacheKey) {
        try {
            String raw = redisTemplate.opsForValue().get(cacheKey);
            return raw == null ? null : objectMapper.readValue(raw, CachedResponse.class);
        } catch (Exception e) {
            log.warn("幂等缓存读取失败，按首次请求处理: {}", e.getMessage());
            return null;
        }
    }

    private void write(String cacheKey, CachedResponse value) {
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(value), TTL);
        } catch (Exception e) {
            log.warn("幂等缓存写入失败，本次请求不受影响: {}", e.getMessage());
        }
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.reset();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /** Redis 中保存的首次响应快照。 */
    private record CachedResponse(int status, String body) {
    }
}
