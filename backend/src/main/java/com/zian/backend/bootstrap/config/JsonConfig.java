package com.zian.backend.bootstrap.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 2 配置（Spring Boot 3.5.x / Jackson 2.19）。
 *
 * <p>雪花 id 为 19 位 Long，超过 JS Number.MAX_SAFE_INTEGER（2^53）。
 * 统一把 Long / long 序列化为字符串，避免前端精度丢失；前端 TS 中 id 一律声明为 string。
 */
@Configuration
public class JsonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("LongToStringModule");
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modulesToInstall(module);
        };
    }
}
