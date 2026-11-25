package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.DeletionRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 读者注销申请仓储端口。
 */
public interface DeletionRequestRepository {

    Optional<DeletionRequest> findById(long id);

    Optional<DeletionRequest> findPending(long userId);

    Optional<DeletionRequest> findLatest(long userId);

    List<DeletionRequest> list(String status, int limit);

    long insert(DeletionRequest request);

    int updateStatus(long id, String status, String note, Long handledBy, LocalDateTime handledAt);
}
