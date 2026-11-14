package com.zian.backend.borrow.infrastructure.persistence;

import com.zian.backend.borrow.domain.repository.RenewRecordRepository;
import com.zian.backend.borrow.infrastructure.persistence.mapper.RenewRecordMapper;
import com.zian.backend.borrow.infrastructure.persistence.po.RenewRecordPO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 续借记录仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RenewRecordRepositoryImpl implements RenewRecordRepository {

    private final RenewRecordMapper mapper;

    @Override
    public void append(
            Long borrowOrderId,
            int seq,
            LocalDate oldDueAt,
            LocalDate newDueAt,
            String result,
            String rejectCode,
            int queueLen,
            String operatorType,
            Long operatorId) {
        RenewRecordPO po = new RenewRecordPO();
        po.setBorrowOrderId(borrowOrderId);
        po.setSeq(seq);
        po.setOldDueAt(oldDueAt);
        po.setNewDueAt(newDueAt);
        po.setResult(result);
        po.setRejectCode(rejectCode);
        po.setQueueLen(queueLen);
        po.setOperatorType(operatorType);
        po.setOperatorId(operatorId);
        po.setCreatedAt(LocalDateTime.now());
        mapper.insert(po);
    }
}
