package com.zian.backend.bootstrap.web;

import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 启动自检端点：确认 Web、MySQL、Redis 三条链路可用。
 */
@Slf4j
@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemHealthMapper healthMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 健康检查：探测 MySQL 与 Redis，并返回基础表数量。
     *
     * @return 服务、数据库与缓存状态
     */
    @GetMapping("/health")
    public BaseResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("application", "readdue-backend");
        data.put("time", Instant.now().toString());

        Integer tableCount = healthMapper.countBaseTables();
        data.put("mysql", "UP");
        data.put("baseTables", tableCount);

        String pong = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
        data.put("redis", "PONG".equalsIgnoreCase(pong) ? "UP" : "UNKNOWN");
        log.debug("health check: tables={}, redis={}", tableCount, pong);
        return ResultUtils.success(data);
    }
}
