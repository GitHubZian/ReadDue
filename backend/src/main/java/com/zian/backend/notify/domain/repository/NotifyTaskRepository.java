package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.NotifyTask;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知任务仓储。
 */
public interface NotifyTaskRepository {

    Long insert(NotifyTask task);

    Optional<NotifyTask> findById(Long id);

    Optional<NotifyTask> findByDedupKey(String dedupKey);

    int update(NotifyTask task);

    /**
     * 到点待投递任务。
     */
    List<NotifyTask> findDue(LocalDateTime now, int limit);

    /**
     * 取消某借阅单下所有尚未投递的提醒。
     */
    int cancelPlannedByOrder(Long borrowOrderId);

    /**
     * 取消某借阅单下指定级别的尚未投递提醒。
     */
    int cancelPlannedByOrderAndLevels(Long borrowOrderId, List<String> levels);
}
