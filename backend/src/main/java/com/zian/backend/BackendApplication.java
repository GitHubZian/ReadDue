package com.zian.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动入口。
 *
 * <p>Mapper 扫描由 MyBatis-Spring-Boot 的自动配置完成：本包下所有带 {@code @Mapper} 的接口都会被注册。
 * 不再使用 {@code @MapperScan}，避免扫描器把 {@code domain/repository} 里没有 SQL 的端口接口
 * 也当成 Mapper 注册成 Bean（会与 {@code XxxRepositoryImpl} 形成同类型多 Bean）。
 */
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
