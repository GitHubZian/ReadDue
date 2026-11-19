package com.zian.backend.notify.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.notify.domain.model.NotifyTask;
import com.zian.backend.notify.domain.model.NotifyTaskStatus;
import com.zian.backend.notify.domain.repository.NotifyTaskRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyTaskMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyTaskPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 通知任务仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class NotifyTaskRepositoryImpl implements NotifyTaskRepository {

    private final NotifyTaskMapper mapper;

    @Override
    public Long insert(NotifyTask task) {
        NotifyTaskPO po = new NotifyTaskPO();
        po.setDedupKey(task.dedupKey());
        po.setReaderId(task.readerId());
        po.setBorrowOrderId(task.borrowOrderId());
        po.setLevel(task.level());
        po.setPlanAt(task.planAt());
        po.setQuietShifted(task.quietShifted() ? 1 : 0);
        po.setStatus(task.status().name());
        po.setPayload(task.payload());
        po.setTopicCode(task.topicCode());
        po.setPrefSnapshot(task.prefSnapshot());
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public Optional<NotifyTask> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<NotifyTask> findByDedupKey(String dedupKey) {
        return mapper.selectList(Wrappers.<NotifyTaskPO>lambdaQuery()
                        .eq(NotifyTaskPO::getDedupKey, dedupKey)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public int update(NotifyTask task) {
        NotifyTaskPO po = new NotifyTaskPO();
        po.setId(task.id());
        po.setPlanAt(task.planAt());
        po.setQuietShifted(task.quietShifted() ? 1 : 0);
        po.setStatus(task.status().name());
        po.setPayload(task.payload());
        return mapper.updateById(po);
    }

    @Override
    public List<NotifyTask> findDue(LocalDateTime now, int limit) {
        return mapper.selectList(Wrappers.<NotifyTaskPO>lambdaQuery()
                        .eq(NotifyTaskPO::getStatus, NotifyTaskStatus.PLANNED.name())
                        .le(NotifyTaskPO::getPlanAt, now)
                        .orderByAsc(NotifyTaskPO::getPlanAt)
                        .last("limit " + Math.max(limit, 1)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int cancelPlannedByOrder(Long borrowOrderId) {
        NotifyTaskPO po = new NotifyTaskPO();
        po.setStatus(NotifyTaskStatus.CANCELED.name());
        return mapper.update(
                po,
                Wrappers.<NotifyTaskPO>lambdaQuery()
                        .eq(NotifyTaskPO::getBorrowOrderId, borrowOrderId)
                        .eq(NotifyTaskPO::getStatus, NotifyTaskStatus.PLANNED.name()));
    }

    @Override
    public int cancelPlannedByOrderAndLevels(Long borrowOrderId, List<String> levels) {
        NotifyTaskPO po = new NotifyTaskPO();
        po.setStatus(NotifyTaskStatus.CANCELED.name());
        return mapper.update(
                po,
                Wrappers.<NotifyTaskPO>lambdaQuery()
                        .eq(NotifyTaskPO::getBorrowOrderId, borrowOrderId)
                        .in(NotifyTaskPO::getLevel, levels)
                        .eq(NotifyTaskPO::getStatus, NotifyTaskStatus.PLANNED.name()));
    }

    private NotifyTask toDomain(NotifyTaskPO po) {
        return new NotifyTask(
                po.getId(),
                po.getDedupKey(),
                po.getReaderId(),
                po.getBorrowOrderId(),
                po.getLevel(),
                po.getPlanAt(),
                po.getQuietShifted() != null && po.getQuietShifted() == 1,
                NotifyTaskStatus.valueOf(po.getStatus()),
                po.getPayload(),
                po.getTopicCode(),
                po.getPrefSnapshot(),
                po.getCreatedAt());
    }
}
