package com.zian.backend.audit.infrastructure.persistence;

import com.zian.backend.audit.domain.model.AuditRecord;
import com.zian.backend.audit.domain.repository.AuditLogRepository;
import com.zian.backend.audit.infrastructure.persistence.mapper.AuditLogMapper;
import com.zian.backend.audit.infrastructure.persistence.po.AuditLogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 审计日志仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogMapper mapper;

    @Override
    public void append(AuditRecord record) {
        AuditLogPO po = new AuditLogPO();
        po.setActorId(record.actorId());
        po.setActorRole(record.actorRole());
        po.setAction(record.action());
        po.setObjectType(record.objectType());
        po.setObjectId(record.objectId() == null ? 0L : record.objectId());
        po.setPurpose(record.purpose());
        po.setDetail(record.detail());
        po.setIp(record.ip());
        po.setTraceId(record.traceId());
        po.setBeforeJson(record.before());
        po.setAfterJson(record.after());
        mapper.insert(po);
    }
}
