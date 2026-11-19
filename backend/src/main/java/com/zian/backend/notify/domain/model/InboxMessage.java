package com.zian.backend.notify.domain.model;

import java.time.LocalDateTime;

/**
 * 站内信一条（已成功投递到 INBOX 的通知）。
 *
 * <p>只读视图，由读模型直接映射，不参与状态变更；{@code readAt} 为空表示未读。
 *
 * @param id          投递记录 ID（站内信唯一标识）
 * @param level       通知级别，见 {@code NotifyService.LEVEL_*}
 * @param orderId     关联借阅单 ID，可为空
 * @param bookTitle   关联书目名称，可为空（非借阅类通知没有）
 * @param overdueDays 逾期天数，仅 L3 有值
 * @param sentAt      投递时间
 * @param readAt      阅读时间，空表示未读
 */
public record InboxMessage(
        Long id,
        String level,
        Long orderId,
        String bookTitle,
        Integer overdueDays,
        LocalDateTime sentAt,
        LocalDateTime readAt) {

    /**
     * 是否未读。
     *
     * @return 未读返回 true
     */
    public boolean unread() {
        return readAt == null;
    }
}
