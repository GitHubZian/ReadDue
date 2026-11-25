package com.zian.backend.iam.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.iam.domain.model.DeletionRequest;
import com.zian.backend.iam.domain.repository.DeletionRequestRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.DeletionRequestMapper;
import com.zian.backend.iam.infrastructure.persistence.po.DeletionRequestPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读者注销申请仓储实现（MyBatis-Plus，M6）。
 */
@Repository
@RequiredArgsConstructor
public class DeletionRequestRepositoryImpl implements DeletionRequestRepository {

    private final DeletionRequestMapper mapper;

    @Override
    public Optional<DeletionRequest> findById(long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<DeletionRequest> findPending(long userId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<DeletionRequestPO>lambdaQuery()
                        .eq(DeletionRequestPO::getUserId, userId)
                        .eq(DeletionRequestPO::getStatus, "PENDING")
                        .orderByDesc(DeletionRequestPO::getCreatedAt)
                        .last("LIMIT 1")))
                .map(this::toDomain);
    }

    @Override
    public Optional<DeletionRequest> findLatest(long userId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<DeletionRequestPO>lambdaQuery()
                        .eq(DeletionRequestPO::getUserId, userId)
                        .orderByDesc(DeletionRequestPO::getCreatedAt)
                        .last("LIMIT 1")))
                .map(this::toDomain);
    }

    @Override
    public List<DeletionRequest> list(String status, int limit) {
        return mapper.selectList(Wrappers.<DeletionRequestPO>lambdaQuery()
                        .eq(status != null && !status.isBlank(), DeletionRequestPO::getStatus, status)
                        .orderByDesc(DeletionRequestPO::getCreatedAt)
                        .last("LIMIT " + limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long insert(DeletionRequest request) {
        DeletionRequestPO po = new DeletionRequestPO();
        po.setUserId(request.userId());
        po.setStatus(request.status());
        po.setReason(request.reason());
        po.setNote(request.note());
        po.setCreatedAt(request.createdAt());
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public int updateStatus(long id, String status, String note, Long handledBy, LocalDateTime handledAt) {
        return mapper.update(null, Wrappers.<DeletionRequestPO>lambdaUpdate()
                .eq(DeletionRequestPO::getId, id)
                .eq(DeletionRequestPO::getStatus, "PENDING")
                .set(DeletionRequestPO::getStatus, status)
                .set(DeletionRequestPO::getNote, note)
                .set(DeletionRequestPO::getHandledBy, handledBy)
                .set(DeletionRequestPO::getHandledAt, handledAt));
    }

    private DeletionRequest toDomain(DeletionRequestPO po) {
        return new DeletionRequest(
                po.getId(),
                po.getUserId(),
                po.getStatus(),
                po.getReason(),
                po.getNote(),
                po.getCreatedAt(),
                po.getHandledAt(),
                po.getHandledBy());
    }
}
