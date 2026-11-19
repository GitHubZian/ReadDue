package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.NotifyPreference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 读者通知偏好与变更日志。
 */
public interface NotifyPreferenceRepository {

    /**
     * 读者全部自定义偏好。
     *
     * @param userId 读者用户 ID
     * @return 偏好列表（只含与默认不同的主题）
     */
    List<NotifyPreference> findByUser(long userId);

    /**
     * 读者对某个主题的偏好。
     *
     * @param userId 读者用户 ID
     * @param topicId 主题 ID
     * @return 无自定义时为空
     */
    Optional<NotifyPreference> find(long userId, long topicId);

    /**
     * 写入或覆盖偏好。
     *
     * @param userId 读者用户 ID
     * @param topicId 主题 ID
     * @param frequency 频率
     * @param channels 渠道
     */
    void upsert(long userId, long topicId, String frequency, List<String> channels);

    /**
     * 删除偏好（恢复组织默认）。
     *
     * @param userId 读者用户 ID
     * @param topicId 主题 ID
     */
    void delete(long userId, long topicId);

    /**
     * 追加变更日志。
     *
     * @param userId 读者用户 ID
     * @param topicCode 主题编码
     * @param beforeJson 变更前 JSON，可为 null
     * @param afterJson 变更后 JSON，可为 null
     * @param operatorId 操作者 ID
     */
    void appendLog(long userId, String topicCode, String beforeJson, String afterJson, Long operatorId);

    /**
     * 读者最近的变更日志。
     *
     * @param userId 读者用户 ID
     * @param limit 条数上限
     * @return 按时间倒序
     */
    List<PreferenceLog> logs(long userId, int limit);

    /**
     * 一条偏好变更日志。
     *
     * @param topicCode 主题编码
     * @param beforeJson 变更前 JSON
     * @param afterJson 变更后 JSON
     * @param createdAt 时间
     */
    record PreferenceLog(String topicCode, String beforeJson, String afterJson, LocalDateTime createdAt) {}
}
