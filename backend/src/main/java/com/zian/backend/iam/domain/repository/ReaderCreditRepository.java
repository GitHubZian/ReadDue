package com.zian.backend.iam.domain.repository;

/**
 * 读者信用仓储。
 */
public interface ReaderCreditRepository {

    /**
     * 读取信用档位（0–3），未建档返回 0。
     */
    int ktTier(Long userId);
}
