package com.zian.backend.notify.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.notify.domain.model.NotifyTopic;
import com.zian.backend.notify.domain.repository.NotifyTopicRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyTopicMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyTopicPO;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 通知主题仓储：渠道 JSON 在边界序列化/反序列化。
 */
@Repository
@RequiredArgsConstructor
public class NotifyTopicRepositoryImpl implements NotifyTopicRepository {

    private static final TypeReference<List<String>> CHANNELS = new TypeReference<>() {};

    private final NotifyTopicMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<NotifyTopic> listAll() {
        return mapper.selectList(Wrappers.<NotifyTopicPO>lambdaQuery().orderByAsc(NotifyTopicPO::getSort))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NotifyTopic> findByCode(String code) {
        return mapper.selectList(Wrappers.<NotifyTopicPO>lambdaQuery()
                        .eq(NotifyTopicPO::getCode, code)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public void updateDefaults(long topicId, String frequency, List<String> channels, boolean enabled) {
        NotifyTopicPO po = new NotifyTopicPO();
        po.setId(topicId);
        po.setDefaultFrequency(frequency);
        po.setDefaultChannels(write(channels));
        po.setEnabled(enabled ? 1 : 0);
        mapper.updateById(po);
    }

    private NotifyTopic toDomain(NotifyTopicPO po) {
        return new NotifyTopic(
                po.getId(),
                po.getCode(),
                po.getName(),
                po.getPriority(),
                po.getDefaultFrequency(),
                read(po.getDefaultChannels()),
                Integer.valueOf(1).equals(po.getEnabled()),
                po.getSort() == null ? 0 : po.getSort());
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
