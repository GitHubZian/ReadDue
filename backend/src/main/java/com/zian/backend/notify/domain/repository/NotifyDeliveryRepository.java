package com.zian.backend.notify.domain.repository;

/**
 * 通知投递记录仓储。
 */
public interface NotifyDeliveryRepository {

    /**
     * 写入一条投递记录，成功时补充发送时间与供应商消息 ID。
     *
     * @param providerMsgId SMTP Message-ID；未发送或不可用时为 null
     */
    void append(
            Long taskId,
            String channel,
            String status,
            int attempt,
            String errorCode,
            String errorMsg,
            String providerMsgId);

    /**
     * 兼容旧调用：不记录供应商消息 ID。
     */
    default void append(Long taskId, String channel, String status, int attempt, String errorCode, String errorMsg) {
        append(taskId, channel, status, attempt, errorCode, errorMsg, null);
    }
}
