package com.zian.backend.notify.infrastructure.persistence;

import com.zian.backend.notify.domain.repository.NotifyDeliveryRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyDeliveryMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyDeliveryPO;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 通知投递记录仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class NotifyDeliveryRepositoryImpl implements NotifyDeliveryRepository {

    private final NotifyDeliveryMapper mapper;

    @Override
    public void append(
            Long taskId,
            String channel,
            String status,
            int attempt,
            String errorCode,
            String errorMsg,
            String providerMsgId) {
        NotifyDeliveryPO po = new NotifyDeliveryPO();
        po.setTaskId(taskId);
        po.setChannel(channel);
        po.setStatus(status);
        po.setAttempt(attempt);
        po.setErrorCode(errorCode);
        po.setErrorMsg(errorMsg);
        po.setProviderMsgId(providerMsgId);
        po.setSentAt("SENT".equals(status) ? LocalDateTime.now() : null);
        mapper.insert(po);
    }
}
