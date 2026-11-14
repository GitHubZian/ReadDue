package com.zian.backend.notify.domain.repository;

/**
 * 通知投递记录仓储。
 */
public interface NotifyDeliveryRepository {

    /**
     * 写入一条投递记录，成功时补充发送时间。
     */
    void append(Long taskId, String channel, String status, int attempt, String errorCode, String errorMsg);
}
