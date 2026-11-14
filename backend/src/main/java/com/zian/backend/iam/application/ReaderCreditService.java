package com.zian.backend.iam.application;

import com.zian.backend.iam.domain.repository.ReaderCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者信用档位查询：宽限公式的 Kt 系数按档位映射规则参数。
 */
@Service
@RequiredArgsConstructor
public class ReaderCreditService {

    private final ReaderCreditRepository readerCreditRepository;

    /**
     * 读取读者信用档位（0–3），未建档按 0 处理。
     *
     * @param userId 用户 ID
     * @return 档位
     */
    @Transactional(readOnly = true)
    public int ktTier(Long userId) {
        return readerCreditRepository.ktTier(userId);
    }
}
