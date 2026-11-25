package com.zian.backend.bootstrap.demo;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 启动守卫：打印演示数据开关状态，生产 profile 下禁止开启演示数据。
 */
@Slf4j
@Component
public class DemoSeedGuard implements InitializingBean {

    private final Environment environment;
    private final boolean demoEnabled;

    public DemoSeedGuard(Environment environment, @Value("${app.demo.enabled:false}") boolean demoEnabled) {
        this.environment = environment;
        this.demoEnabled = demoEnabled;
    }

    @Override
    public void afterPropertiesSet() {
        String[] profiles = environment.getActiveProfiles();
        log.info("启动配置检查：activeProfiles={}，app.demo.enabled={}", Arrays.toString(profiles), demoEnabled);
        boolean prod = Arrays.stream(profiles)
                .anyMatch(profile -> profile != null && profile.toLowerCase().contains("prod"));
        if (prod && demoEnabled) {
            throw new IllegalStateException("生产环境禁止开启演示数据：请设置 app.demo.enabled=false");
        }
    }
}
