package com.zian.backend.notify.application;

import com.zian.backend.notify.domain.model.NotifyChannel;
import com.zian.backend.notify.domain.model.NotifyFrequency;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * FR-11 偏好校验：频率/渠道取值与「优先级 × 频率 × 渠道」硬约束。
 *
 * <p>规则来源 {@code AGENT.md} §10：P0 不可关闭站内信、P2 不允许实时、SMS/APP_PUSH 暂不可用。
 */
final class NotifyPreferenceRules {

    private NotifyPreferenceRules() {
    }

    /**
     * 解析并校验频率。
     *
     * @param raw 客户端传入值
     * @return 规范化后的大写频率
     * @throws BusinessException 为空或不在枚举内（400）
     */
    static String frequency(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "frequency 不能为空");
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (!NotifyFrequency.ALL.contains(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "frequency 只能是 REALTIME、DAILY 或 WEEKLY");
        }
        return value;
    }

    /**
     * 解析并校验渠道列表。
     *
     * @param raw 客户端传入渠道
     * @return 去重后的大写渠道
     * @throws BusinessException 为空（400）或渠道暂不可用（422）
     */
    static List<String> channels(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "channels 不能为空");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String item : raw) {
            if (item == null || item.isBlank()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "channels 不能包含空值");
            }
            String value = item.trim().toUpperCase(Locale.ROOT);
            if (!NotifyChannel.SUPPORTED.contains(value)) {
                throw new BusinessException(
                        ErrorCode.RULE_VIOLATION, "渠道暂不可用：" + value + "，目前仅支持 INBOX、EMAIL");
            }
            normalized.add(value);
        }
        return new ArrayList<>(normalized);
    }

    /**
     * 校验主题优先级带来的硬约束。
     *
     * @param priority  主题优先级 P0/P1/P2
     * @param frequency 已规范化频率
     * @param channels  已规范化渠道
     * @throws BusinessException P0 缺站内信或 P2 使用实时（422）
     */
    static void constrain(String priority, String frequency, List<String> channels) {
        if ("P0".equals(priority) && !channels.contains(NotifyChannel.INBOX)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该主题为必达通知，站内信不可关闭");
        }
        if ("P2".equals(priority) && NotifyFrequency.REALTIME.equals(frequency)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该主题不支持实时通知，请选择 DAILY 或 WEEKLY");
        }
    }
}
