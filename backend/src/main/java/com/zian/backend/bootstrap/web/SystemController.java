package com.zian.backend.bootstrap.web;

import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 启动自检端点：确认 Web、MySQL、Redis 三条链路可用。
 * 后续迁移到 bootstrap/interfaces，仅用于本地与运维探活。
 */
@Slf4j
@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/health")
    public BaseResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("application", "readdue-backend");
        data.put("time", Instant.now().toString());

        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'",
                Integer.class);
        data.put("mysql", "UP");
        data.put("baseTables", tableCount);

        String pong = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
        data.put("redis", "PONG".equalsIgnoreCase(pong) ? "UP" : "UNKNOWN");
        log.debug("health check: tables={}, redis={}", tableCount, pong);
        return ResultUtils.success(data);
    }
}
