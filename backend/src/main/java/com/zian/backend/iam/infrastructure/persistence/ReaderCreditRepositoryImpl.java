package com.zian.backend.iam.infrastructure.persistence;

import com.zian.backend.iam.domain.repository.ReaderCreditRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.ReaderCreditMapper;
import com.zian.backend.iam.infrastructure.persistence.po.ReaderCreditPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读者信用仓储实现（主键即 user_id）。
 */
@Repository
@RequiredArgsConstructor
public class ReaderCreditRepositoryImpl implements ReaderCreditRepository {

    private final ReaderCreditMapper mapper;

    @Override
    public int ktTier(Long userId) {
        ReaderCreditPO po = mapper.selectById(userId);
        return po == null || po.getKtTier() == null ? 0 : po.getKtTier();
    }
}
