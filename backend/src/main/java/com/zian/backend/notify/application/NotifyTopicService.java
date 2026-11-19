package com.zian.backend.notify.application;

import com.zian.backend.notify.domain.model.NotifyTopic;
import com.zian.backend.notify.domain.repository.NotifyTopicRepository;
import com.zian.backend.notify.interfaces.dto.NotifyTopicDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理端通知主题字典：查看与调整组织默认值。
 *
 * <p>默认值只影响未自定义的读者；已排程任务沿用排程时的快照。
 */
@Service
@RequiredArgsConstructor
public class NotifyTopicService {

    private final NotifyTopicRepository topicRepository;

    /**
     * 全部主题（含下线）。
     *
     * @return 按 sort 升序
     */
    public List<NotifyTopicDtos.AdminTopicView> list() {
        return topicRepository.listAll().stream().map(NotifyTopicService::view).toList();
    }

    /**
     * 更新组织默认频率/渠道与上线状态；字段为空表示保持不变。
     *
     * @param code 主题编码
     * @param request 新默认值
     * @return 更新后的主题
     * @throws BusinessException 主题不存在（404）、参数非法（400）、违反硬约束（422）
     */
    @Transactional
    public NotifyTopicDtos.AdminTopicView updateDefaults(
            String code, NotifyTopicDtos.UpdateDefaultsRequest request) {
        NotifyTopic topic = topicRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "通知主题不存在：" + code));
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
        }
        String frequency = request.defaultFrequency() == null
                ? topic.defaultFrequency()
                : NotifyPreferenceRules.frequency(request.defaultFrequency());
        List<String> channels = request.defaultChannels() == null
                ? topic.defaultChannels()
                : NotifyPreferenceRules.channels(request.defaultChannels());
        NotifyPreferenceRules.constrain(topic.priority(), frequency, channels);
        boolean enabled = request.enabled() == null ? topic.enabled() : request.enabled();
        topicRepository.updateDefaults(topic.id(), frequency, channels, enabled);
        return view(new NotifyTopic(
                topic.id(), topic.code(), topic.name(), topic.priority(), frequency, channels, enabled, topic.sort()));
    }

    private static NotifyTopicDtos.AdminTopicView view(NotifyTopic topic) {
        return new NotifyTopicDtos.AdminTopicView(
                topic.code(),
                topic.name(),
                topic.priority(),
                topic.defaultFrequency(),
                topic.defaultChannels(),
                topic.enabled(),
                topic.sort());
    }
}
