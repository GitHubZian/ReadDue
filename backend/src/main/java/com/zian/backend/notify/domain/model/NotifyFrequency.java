package com.zian.backend.notify.domain.model;

import java.util.Set;

/**
 * 通知频率取值。
 */
public final class NotifyFrequency {

    /** 实时推送。 */
    public static final String REALTIME = "REALTIME";

    /** 每日合并。 */
    public static final String DAILY = "DAILY";

    /** 每周合并。 */
    public static final String WEEKLY = "WEEKLY";

    /** 全部合法取值。 */
    public static final Set<String> ALL = Set.of(REALTIME, DAILY, WEEKLY);

    private NotifyFrequency() {}
}
