package com.zian.backend.notify.domain.model;

import java.util.List;

/**
 * FR-11 通知主题：优先级 + 组织默认频率与渠道。
 *
 * @param id               主键
 * @param code             主题编码
 * @param name             展示名
 * @param priority         P0（不可关）/P1/P2
 * @param defaultFrequency 组织默认频率：REALTIME/DAILY/WEEKLY
 * @param defaultChannels  组织默认渠道
 * @param enabled          主题是否上线
 * @param sort             展示排序
 */
public record NotifyTopic(
        Long id,
        String code,
        String name,
        String priority,
        String defaultFrequency,
        List<String> defaultChannels,
        boolean enabled,
        int sort) {

    /** P0 主题不允许读者关闭站内信。 */
    public boolean locked() {
        return "P0".equals(priority);
    }
}
