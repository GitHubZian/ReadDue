package com.zian.backend.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 口令编码器。
 *
 * <p>独立于 {@code SecurityConfig} 声明：安全过滤链依赖 {@code JwtAuthenticationFilter}，而后者依赖
 * {@code AuthService}、再依赖本 Bean；若本 Bean 与过滤链同处一个配置类，会形成配置类自引用循环。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
