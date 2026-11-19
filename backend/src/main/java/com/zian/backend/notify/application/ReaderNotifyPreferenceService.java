package com.zian.backend.notify.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.notify.domain.model.NotifyPreference;
import com.zian.backend.notify.domain.model.NotifyTopic;
import com.zian.backend.notify.domain.repository.NotifyPreferenceRepository;
import com.zian.backend.notify.domain.repository.NotifyTopicRepository;
import com.zian.backend.notify.interfaces.dto.NotifyPreferenceDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者通知偏好：主题 + 生效值、差异列表、批量更新与变更日志。
 *
 * <p>解析顺序为「读者差异 → 组织默认」；P0/P2 的硬约束见 {@link NotifyPreferenceRules}。
 */
@Service
@RequiredArgsConstructor
public class ReaderNotifyPreferenceService {

    private final NotifyTopicRepository topicRepository;
    private final NotifyPreferenceRepository preferenceRepository;
    private final ObjectMapper objectMapper;

    /**
     * 全部上线主题及当前生效偏好。
     *
     * @return 按 sort 升序
     */
    public List<NotifyPreferenceDtos.TopicView> topics() {
        long userId = UserContext.requireId();
        Map<Long, NotifyPreference> mine = preferenceRepository.findByUser(userId).stream()
                .collect(Collectors.toMap(NotifyPreference::topicId, Function.identity(), (a, b) -> b));
        return topicRepository.listAll().stream()
                .filter(NotifyTopic::enabled)
                .map(topic -> {
                    NotifyPreference custom = mine.get(topic.id());
                    return new NotifyPreferenceDtos.TopicView(
                            topic.code(),
                            topic.name(),
                            topic.priority(),
                            topic.locked(),
                            custom == null ? topic.defaultFrequency() : custom.frequency(),
                            custom == null ? topic.defaultChannels() : custom.channels(),
                            custom != null);
                })
                .toList();
    }

    /**
     * 只返回与组织默认不同的偏好。
     *
     * @return 自定义偏好列表
     */
    public List<NotifyPreferenceDtos.PreferenceView> preferences() {
        long userId = UserContext.requireId();
        Map<Long, NotifyTopic> topics = topicRepository.listAll().stream()
                .collect(Collectors.toMap(NotifyTopic::id, Function.identity()));
        return preferenceRepository.findByUser(userId).stream()
                .map(preference -> {
                    NotifyTopic topic = topics.get(preference.topicId());
                    return topic == null
                            ? null
                            : new NotifyPreferenceDtos.PreferenceView(
                                    topic.code(), preference.frequency(), preference.channels());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * 批量更新偏好；与默认相同则删除差异行。
     *
     * @param request 至少一条待更新项
     * @return 变更条数与最新差异列表
     * @throws BusinessException 主题不存在（404）、参数非法（400）、违反硬约束（422）
     */
    @Transactional
    public NotifyPreferenceDtos.UpdateResult update(NotifyPreferenceDtos.UpdateRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "items 不能为空");
        }
        long userId = UserContext.requireId();
        int updated = 0;
        for (NotifyPreferenceDtos.UpdateItem item : request.items()) {
            if (item == null || item.topicCode() == null || item.topicCode().isBlank()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "topicCode 不能为空");
            }
            String code = item.topicCode().trim().toUpperCase(java.util.Locale.ROOT);
            NotifyTopic topic = topicRepository.findByCode(code)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "通知主题不存在：" + code));
            if (!topic.enabled()) {
                throw new BusinessException(ErrorCode.RULE_VIOLATION, "该通知主题已下线");
            }
            String frequency = NotifyPreferenceRules.frequency(item.frequency());
            List<String> channels = NotifyPreferenceRules.channels(item.channels());
            NotifyPreferenceRules.constrain(topic.priority(), frequency, channels);

            NotifyPreference existing = preferenceRepository.find(userId, topic.id()).orElse(null);
            String before = existing == null ? null : write(existing.frequency(), existing.channels());
            boolean sameAsDefault = topic.defaultFrequency().equals(frequency)
                    && Set.copyOf(topic.defaultChannels()).equals(Set.copyOf(channels));
            if (sameAsDefault) {
                if (existing != null) {
                    preferenceRepository.delete(userId, topic.id());
                    preferenceRepository.appendLog(userId, code, before, null, userId);
                    updated++;
                }
            } else {
                preferenceRepository.upsert(userId, topic.id(), frequency, channels);
                preferenceRepository.appendLog(userId, code, before, write(frequency, channels), userId);
                updated++;
            }
        }
        return new NotifyPreferenceDtos.UpdateResult(updated, preferences());
    }

    /**
     * 最近的偏好变更日志。
     *
     * @param limit 条数，1–100，默认 20
     * @return 按时间倒序
     */
    public List<NotifyPreferenceDtos.LogView> logs(Integer limit) {
        long userId = UserContext.requireId();
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        return preferenceRepository.logs(userId, size).stream()
                .map(log -> new NotifyPreferenceDtos.LogView(
                        log.topicCode(), log.beforeJson(), log.afterJson(), Times.iso(log.createdAt())))
                .toList();
    }

    private String write(String frequency, List<String> channels) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("frequency", frequency);
        value.put("channels", channels);
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("偏好快照序列化失败", e);
        }
    }
}
