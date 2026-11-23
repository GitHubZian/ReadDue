package com.zian.backend.iam.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.iam.domain.model.UserStatus;
import com.zian.backend.iam.domain.repository.UserStatusLogRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.UserStatusLogMapper;
import com.zian.backend.iam.infrastructure.persistence.po.UserStatusLogPO;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 账号状态变更日志仓储实现（MyBatis-Plus）。
 */
@Repository
@RequiredArgsConstructor
public class UserStatusLogRepositoryImpl implements UserStatusLogRepository {

    private final UserStatusLogMapper mapper;

    @Override
    public void append(
            Long userId,
            UserStatus from,
            UserStatus to,
            String reason,
            LocalDateTime expiresAt,
            Long operatorId,
            String operatorName) {
        UserStatusLogPO po = new UserStatusLogPO();
        po.setId(IdWorker.getId());
        po.setUserId(userId);
        po.setFromStatus(from.name());
        po.setToStatus(to.name());
        po.setReason(reason);
        po.setExpiresAt(expiresAt);
        po.setOperatorId(operatorId);
        po.setOperatorName(operatorName);
        po.setCreatedAt(LocalDateTime.now());
        mapper.insert(po);
    }
}
