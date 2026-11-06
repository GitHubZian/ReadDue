package com.zian.backend.shared.outbox.domain.repository;

import com.zian.backend.shared.outbox.domain.model.OutboxEvent;
import java.util.List;

/**
 * 本地消息表仓储。
 */
public interface OutboxRepository {

    /** 业务事务内追加一条待投递事件 */
    void append(OutboxEvent event);

    /** 扫描待投递事件（NEW / FAILED），按创建时间升序 */
    List<OutboxEvent> findPending(int limit);

    /** 投递成功 */
    void markPublished(Long id);

    /** 投递失败：更新状态与重试次数 */
    void markFailed(OutboxEvent event);
}
