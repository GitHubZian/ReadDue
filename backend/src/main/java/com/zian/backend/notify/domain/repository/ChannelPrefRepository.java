package com.zian.backend.notify.domain.repository;

/**
 * 读者渠道偏好。
 */
public interface ChannelPrefRepository {

    /**
     * 判断读者是否开启邮件渠道（无记录视为开启）。
     */
    boolean emailEnabled(Long userId);
}
