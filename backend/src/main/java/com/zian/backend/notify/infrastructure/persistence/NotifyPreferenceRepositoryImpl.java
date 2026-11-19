package com.zian.backend.notify.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.notify.domain.model.NotifyPreference;
import com.zian.backend.notify.domain.repository.NotifyPreferenceRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyPreferenceLogMapper;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyPreferenceMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyPreferenceLogPO;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyPreferencePO;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读者偏好仓储：差异行 upsert，同时写变更日志。
 */
@Repository
@RequiredArgsConstructor
public class NotifyPreferenceRepositoryImpl implements NotifyPreferenceRepository {

    private static final TypeReference<List<String>> CHANNELS = new TypeReference<>() {};

    private final NotifyPreferenceMapper mapper;
    private final NotifyPreferenceLogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<NotifyPreference> findByUser(long userId) {
        return mapper.selectList(Wrappers.<NotifyPreferencePO>lambdaQuery()
                        .eq(NotifyPreferencePO::getUserId, userId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NotifyPreference> find(long userId, long topicId) {
        return mapper.selectList(Wrappers.<NotifyPreferencePO>lambdaQuery()
                        .eq(NotifyPreferencePO::getUserId, userId)
                        .eq(NotifyPreferencePO::getTopicId, topicId)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public void upsert(long userId, long topicId, String frequency, List<String> channels) {
        NotifyPreferencePO existing = mapper.selectList(Wrappers.<NotifyPreferencePO>lambdaQuery()
                        .eq(NotifyPreferencePO::getUserId, userId)
                        .eq(NotifyPreferencePO::getTopicId, topicId)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .orElse(null);
        if (existing == null) {
            NotifyPreferencePO po = new NotifyPreferencePO();
            po.setUserId(userId);
            po.setTopicId(topicId);
            po.setFrequency(frequency);
            po.setChannels(write(channels));
            mapper.insert(po);
            return;
        }
        existing.setFrequency(frequency);
        existing.setChannels(write(channels));
        mapper.updateById(existing);
    }

    @Override
    public void delete(long userId, long topicId) {
        mapper.delete(Wrappers.<NotifyPreferencePO>lambdaQuery()
                .eq(NotifyPreferencePO::getUserId, userId)
                .eq(NotifyPreferencePO::getTopicId, topicId));
    }

    @Override
    public void appendLog(long userId, String topicCode, String beforeJson, String afterJson, Long operatorId) {
        NotifyPreferenceLogPO po = new NotifyPreferenceLogPO();
        po.setUserId(userId);
        po.setTopicCode(topicCode);
        po.setBeforeJson(beforeJson);
        po.setAfterJson(afterJson);
        po.setOperatorId(operatorId);
        logMapper.insert(po);
    }

    @Override
    public List<PreferenceLog> logs(long userId, int limit) {
        return logMapper.selectList(Wrappers.<NotifyPreferenceLogPO>lambdaQuery()
                        .eq(NotifyPreferenceLogPO::getUserId, userId)
                        .orderByDesc(NotifyPreferenceLogPO::getCreatedAt)
                        .last("limit " + Math.max(1, Math.min(limit, 100))))
                .stream()
                .map(po -> new PreferenceLog(
                        po.getTopicCode(), po.getBeforeJson(), po.getAfterJson(), po.getCreatedAt()))
                .toList();
    }

    private NotifyPreference toDomain(NotifyPreferencePO po) {
        return new NotifyPreference(po.getId(), po.getUserId(), po.getTopicId(), po.getFrequency(), read(po.getChannels()));
    }

    private List<String> read(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, CHANNELS);
        } catch (Exception e) {
            return List.of();
        }
    }

    private String write(List<String> channels) {
        try {
            return objectMapper.writeValueAsString(channels);
        } catch (Exception e) {
            throw new IllegalStateException("通知渠道序列化失败", e);
        }
    }
}
