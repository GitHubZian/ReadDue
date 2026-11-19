package com.zian.backend.notify.domain.model;

import java.util.List;

/**
 * 通知渠道取值：本期只有 INBOX / EMAIL 有发送实现。
 */
public final class NotifyChannel {

    /** 站内信。 */
    public static final String INBOX = "INBOX";

    /** 邮件。 */
    public static final String EMAIL = "EMAIL";

    /** 短信（枚举保留，接口调用返回 422）。 */
    public static final String SMS = "SMS";

    /** App 推送（枚举保留，暂无实现）。 */
    public static final String APP_PUSH = "APP_PUSH";

    /** 本期可用的渠道。 */
    public static final List<String> SUPPORTED = List.of(INBOX, EMAIL);

    private NotifyChannel() {}
}
