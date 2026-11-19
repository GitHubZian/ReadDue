package com.zian.backend.notify.interfaces.dto;

import java.util.List;

/**
 * 读者消息中心接口模型。
 *
 * <p>ID 一律用字符串传输：全局配置把 Long 序列化成字符串以防 JS 精度丢失，请求侧也按字符串接收。
 */
public final class ReaderNotifyDtos {

    private ReaderNotifyDtos() {
    }

    /**
     * 一条站内信。
     *
     * @param id      站内信 ID
     * @param level   通知级别（L1/L2/L3/GRACE_RESULT/APPEAL_SLA/RESERVATION_READY/SYSTEM）
     * @param title   标题
     * @param body    正文
     * @param orderId 关联借阅单 ID，可为空
     * @param sentAt  投递时间（+08:00）
     * @param read    是否已读
     */
    public record NotificationItem(
            String id, String level, String title, String body, String orderId, String sentAt, boolean read) {
    }

    /**
     * 未读数。
     *
     * @param unread 未读条数
     */
    public record UnreadCount(int unread) {
    }

    /**
     * 标记已读请求：{@code all=true} 表示全部已读；否则按 {@code ids} 精确标记。
     *
     * @param ids 站内信 ID 列表
     * @param all 是否全部已读
     */
    public record MarkReadRequest(List<String> ids, Boolean all) {
    }

    /**
     * 标记已读结果。
     *
     * @param updated 实际更新条数（已读的不会重复计数）
     */
    public record MarkReadResult(int updated) {
    }
}
