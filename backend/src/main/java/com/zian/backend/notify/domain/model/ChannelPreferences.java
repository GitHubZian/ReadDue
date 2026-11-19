package com.zian.backend.notify.domain.model;

/**
 * 读者可自助开关的提醒渠道。
 *
 * <p>短信与 App 推送尚未接入发送实现，暂不暴露给读者，避免出现「打开了但没有效果」的开关。
 *
 * @param email 是否接收邮件提醒
 * @param inbox 是否接收站内信
 */
public record ChannelPreferences(boolean email, boolean inbox) {

    /** 未建档时的默认值：两个渠道都开启。 */
    public static ChannelPreferences defaults() {
        return new ChannelPreferences(true, true);
    }

    /**
     * 在现有偏好上覆盖非空字段。
     *
     * @param newEmail 新邮件开关，null 表示保持原值
     * @param newInbox 新站内信开关，null 表示保持原值
     * @return 合并后的偏好
     */
    public ChannelPreferences merge(Boolean newEmail, Boolean newInbox) {
        return new ChannelPreferences(newEmail == null ? email : newEmail, newInbox == null ? inbox : newInbox);
    }
}
