package com.zian.backend.notify.domain.repository;

/**
 * 通知投递记录仓储。
 */
public interface NotifyDeliveryRepository {

    /**
     * 追加一条投递记录（站内信/邮件）。
     */
    void append(Long taskId, String channel, String status, int attempt, String errorCode, String errorMsg);
}
