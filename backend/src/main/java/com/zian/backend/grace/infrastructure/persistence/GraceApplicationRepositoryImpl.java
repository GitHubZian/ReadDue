package com.zian.backend.grace.infrastructure.persistence;

import com.zian.backend.grace.domain.model.GraceApplication;
import com.zian.backend.grace.domain.model.GraceStatus;
import com.zian.backend.grace.domain.repository.GraceApplicationRepository;
import com.zian.backend.grace.infrastructure.persistence.mapper.GraceApplicationMapper;
import com.zian.backend.grace.infrastructure.persistence.po.GraceApplicationPO;
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
