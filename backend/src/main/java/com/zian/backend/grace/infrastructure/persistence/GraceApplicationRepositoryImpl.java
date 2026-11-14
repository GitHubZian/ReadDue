package com.zian.backend.grace.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zian.backend.grace.domain.model.GraceApplication;
import com.zian.backend.grace.domain.model.GraceStatus;
import com.zian.backend.grace.domain.repository.GraceApplicationRepository;
import com.zian.backend.grace.infrastructure.persistence.mapper.GraceApplicationMapper;
import com.zian.backend.grace.infrastructure.persistence.po.GraceApplicationPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 宽限申请仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class GraceApplicationRepositoryImpl implements GraceApplicationRepository {

    private final GraceApplicationMapper mapper;

    @Override
    public Optional<GraceApplication> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public int update(GraceApplication application) {
        GraceApplicationPO po = new GraceApplicationPO();
        po.setId(application.getId());
        po.setStatus(application.getStatus().name());
        po.setGrantedDays(application.getGrantedDays());
        po.setDecidedBy(application.getDecidedBy());
        po.setDecidedAt(application.getDecidedAt());
        po.setRejectCode(application.getRejectCode());
        po.setVersion(application.getVersion());
        return mapper.updateById(po);
    }

    @Override
    public Long insert(GraceApplication application) {
        GraceApplicationPO po = new GraceApplicationPO();
        po.setApplyNo(application.getApplyNo());
        po.setBorrowOrderId(application.getBorrowOrderId());
        po.setReaderId(application.getReaderId());
        po.setSceneCode(application.getSceneCode());
        po.setSceneSource(application.getSceneSource());
        po.setCalendarHit(application.isCalendarHit() ? 1 : 0);
        po.setBaseDays(application.getBaseDays());
        po.setKt(application.getKt());
        po.setKq(application.getKq());
        po.setDmax(application.getDmax());
        po.setComputedDays(application.getComputedDays());
        po.setEvidenceText(application.getEvidenceText());
        po.setFormulaVersion(application.getFormulaVersion());
        po.setQueueLen(application.getQueueLen());
        po.setSnapshotExpiresAt(application.getSnapshotExpiresAt());
        po.setStatus(application.getStatus().name());
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public List<GraceApplication> findByReader(Long readerId, String status, int offset, int limit) {
        return mapper
                .selectPage(
                        new Page<>(offset / Math.max(1, limit) + 1, limit, false),
                        Wrappers.<GraceApplicationPO>lambdaQuery()
                                .eq(GraceApplicationPO::getReaderId, readerId)
                                .eq(status != null && !status.isBlank(), GraceApplicationPO::getStatus, status)
                                .orderByDesc(GraceApplicationPO::getCreatedAt)
                                .orderByDesc(GraceApplicationPO::getId))
                .getRecords()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByReader(Long readerId, String status) {
        return mapper.selectCount(Wrappers.<GraceApplicationPO>lambdaQuery()
                .eq(GraceApplicationPO::getReaderId, readerId)
                .eq(status != null && !status.isBlank(), GraceApplicationPO::getStatus, status));
    }

    @Override
    public int countApprovedSince(Long borrowOrderId, LocalDateTime since) {
        return Math.toIntExact(mapper.selectCount(Wrappers.<GraceApplicationPO>lambdaQuery()
                .eq(GraceApplicationPO::getBorrowOrderId, borrowOrderId)
                .eq(GraceApplicationPO::getStatus, GraceStatus.APPROVED.name())
                .ge(GraceApplicationPO::getCreatedAt, since)));
    }

    @Override
    public boolean existsPending(Long borrowOrderId) {
        return mapper.exists(Wrappers.<GraceApplicationPO>lambdaQuery()
                .eq(GraceApplicationPO::getBorrowOrderId, borrowOrderId)
                .eq(GraceApplicationPO::getStatus, GraceStatus.PENDING.name()));
    }

    private GraceApplication toDomain(GraceApplicationPO po) {
        return new GraceApplication(
                po.getId(),
                po.getApplyNo(),
                po.getBorrowOrderId(),
                po.getReaderId(),
                po.getSceneCode(),
                po.getSceneSource(),
                po.getCalendarHit() != null && po.getCalendarHit() == 1,
                po.getBaseDays(),
                po.getKt(),
                po.getKq(),
                po.getDmax() == null ? 0 : po.getDmax(),
                po.getComputedDays(),
                po.getGrantedDays(),
                po.getEvidenceText(),
                po.getFormulaVersion(),
                po.getQueueLen() == null ? 0 : po.getQueueLen(),
                po.getSnapshotExpiresAt(),
                GraceStatus.valueOf(po.getStatus()),
                po.getDecidedBy(),
                po.getDecidedAt(),
                po.getRejectCode(),
                po.getVersion());
    }
}
