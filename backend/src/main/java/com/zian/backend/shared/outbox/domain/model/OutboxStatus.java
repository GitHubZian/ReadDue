package com.zian.backend.shared.outbox.domain.model;

/**
 * event_outbox.status 取值。
 *
 * <p>NEW 待投递 / PUBLISHED 已投递 / FAILED 投递失败待重试 / DEAD 超过重试上限等待人工介入。
 */
public final class OutboxStatus {

    public static final String NEW = "NEW";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String FAILED = "FAILED";
    public static final String DEAD = "DEAD";

    private OutboxStatus() {
    }
}
