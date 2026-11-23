package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 账号仓储。
 */
public interface UserRepository {

    /**
     * 按 ID 查用户。
     */

    Optional<User> findById(Long id);

    /**
     * 按学号/工号查用户。
     */

    Optional<User> findByUserNo(String userNo);

    /**
     * 回写账号状态与登录时间，返回影响行数。
     */
    int update(User user);

    /**
     * 查询拉黑已到期的账号，供定时任务恢复。
     *
     * @param now   判定时间
     * @param limit 单批上限
     * @return 已到期账号
     */
    List<User> findExpiredBlacklist(LocalDateTime now, int limit);

}
