package com.zian.backend.bootstrap.demo;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

/**
 * 演示数据启动守卫 + 默认关闭断言。
 */
class DemoSeedGuardTest {

    private Environment profiles(String... names) {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(names);
        return environment;
    }

    @Test
    void prodWithDemoEnabledFailsFast() {
        DemoSeedGuard guard = new DemoSeedGuard(profiles("prod"), true);
        assertThatThrownBy(guard::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("生产环境禁止开启演示数据");
    }

    @Test
    void localWithDemoEnabledStarts() {
        DemoSeedGuard guard = new DemoSeedGuard(profiles("local"), true);
        assertThatCode(guard::afterPropertiesSet).doesNotThrowAnyException();
    }

    @Test
    void initializerIsOptIn() {
        ConditionalOnProperty annotation =
                DemoDataInitializer.class.getAnnotation(ConditionalOnProperty.class);
        org.assertj.core.api.Assertions.assertThat(annotation.matchIfMissing()).isFalse();
        org.springframework.context.annotation.Profile profile =
                DemoDataInitializer.class.getAnnotation(org.springframework.context.annotation.Profile.class);
        org.assertj.core.api.Assertions.assertThat(profile.value()).containsExactly("local");
    }
}
