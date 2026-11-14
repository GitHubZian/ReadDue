package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.NotifyTask;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知任务仓储。
 */
public interface NotifyTaskRepository {

    /**
     * 新建通知任务，返回主键。
     */

    Long insert(NotifyTask task);

    /**
     * 按 ID 查任务。
     */

    Optional<NotifyTask> findById(Long id);

    /**
     * 按去重键查任务，用于幂等。
     */

    Optional<NotifyTask> findByDedupKey(String dedupKey);

    /**
     * 回写任务状态与投递信息。
     */

    int update(NotifyTask task);

    /**
     * 取出到期待投递的任务。
     */
    List<NotifyTask> findDue(LocalDateTime now, int limit);

    /**
     * 取消该借阅单所有未发任务。
     */
    int cancelPlannedByOrder(Long borrowOrderId);

    /**
     * 按级别取消未发任务。
     */
    int cancelPlannedByOrderAndLevels(Long borrowOrderId, List<String> levels);
}
