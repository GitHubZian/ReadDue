package com.zian.backend.shared.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * JWT 密钥强度校验：示例/弱密钥在非 local 环境启动即失败。
 */
class JwtServiceSecretTest {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);

    private Environment profiles(String... names) {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(names);
        return environment;
    }

    private JwtService service(String secret, Environment environment) {
        return new JwtService(secret, "readdue", 7200, redis, environment);
    }

    @Test
    void weakSecretsFailOutsideLocal() {
        assertThatThrownBy(() -> service("change-me", profiles("prod")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service("readdue-dev-secret", profiles("prod")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", profiles("prod")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service("abcdefghabcdefghabcdefghabcdefgh", profiles("prod")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void weakSecretOnlyWarnsLocally() {
        service("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", profiles("local"));
    }
}
