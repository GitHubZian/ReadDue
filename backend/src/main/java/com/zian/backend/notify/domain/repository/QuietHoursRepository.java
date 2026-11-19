package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.QuietHours;
import java.util.Optional;

/**
 * 读者免打扰时段。
 */
public interface QuietHoursRepository {

    /**
     * 读取读者自定义的免打扰时段。
     *
     * @param userId 读者用户 ID
     * @return 无自定义记录时为空
     */
    Optional<QuietHours> find(long userId);

    /**
     * 保存读者免打扰时段（按 user_id upsert）。
     *
     * @param userId 读者用户 ID
     * @param quietHours 目标时段
     */
    void save(long userId, QuietHours quietHours);
}
