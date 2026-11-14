package com.zian.backend.shared.outbox.domain.repository;

import com.zian.backend.shared.outbox.domain.model.OutboxEvent;
import java.util.List;

/**
 * 本地消息表仓储。
 */
public interface OutboxRepository {

    /**
     * 写入待投递事件。
     */
    void append(OutboxEvent event);

    /**
     * 取待投递事件（NEW/FAILED）。
     */
    List<OutboxEvent> findPending(int limit);

    /**
     * 标记事件已投递。
     */
    void markPublished(Long id);

    /**
     * 记录投递失败，超过重试上限转 DEAD。
     */
    void markFailed(OutboxEvent event);
}
