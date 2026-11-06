package com.zian.backend.bootstrap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务开关：outbox 投递器、SLA 超时补偿、提醒扫描都依赖它。
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
