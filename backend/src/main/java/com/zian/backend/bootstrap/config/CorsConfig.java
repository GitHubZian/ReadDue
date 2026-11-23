package com.zian.backend.bootstrap.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置。
 *
 * <p>允许的来源由 {@code app.cors.allowed-origin-patterns} 控制（逗号分隔），
 * 避免生产环境出现「{@code *} + credentials」；留空表示不开放跨域。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;

    public CorsConfig(
            @Value("${app.cors.allowed-origin-patterns:"
                            + "http://localhost:5173,http://127.0.0.1:5173,"
                            + "http://localhost:8080,http://127.0.0.1:8080}")
                    String patterns) {
        this.allowedOriginPatterns = Arrays.stream(patterns.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOriginPatterns.length == 0) {
            return;
        }
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 白名单来源，不使用 "*"
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
