package com.zian.backend.borrow.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.borrow.domain.model.BorrowOrder;
import com.zian.backend.borrow.domain.model.BorrowStatus;
import com.zian.backend.borrow.domain.model.ReturnCondition;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import com.zian.backend.borrow.infrastructure.persistence.mapper.BorrowOrderMapper;
import com.zian.backend.borrow.infrastructure.persistence.po.BorrowOrderPO;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 借阅单仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class BorrowOrderRepositoryImpl implements BorrowOrderRepository {

    private static final List<String> ACTIVE = List.of(BorrowStatus.BORROWED.name(), BorrowStatus.OVERDUE.name());

    private final BorrowOrderMapper mapper;

    @Override
    public Optional<BorrowOrder> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<BorrowOrder> findActiveByCopyId(Long copyId) {
        BorrowOrderPO po = mapper.selectOne(Wrappers.<BorrowOrderPO>lambdaQuery()
                .eq(BorrowOrderPO::getCopyId, copyId)
                .in(BorrowOrderPO::getStatus, ACTIVE)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<BorrowOrder> findActiveByReaderAndBook(Long readerId, Long bookId) {
        BorrowOrderPO po = mapper.selectOne(Wrappers.<BorrowOrderPO>lambdaQuery()
                .eq(BorrowOrderPO::getReaderId, readerId)
                .eq(BorrowOrderPO::getBookId, bookId)
                .in(BorrowOrderPO::getStatus, ACTIVE)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }


    @Override
    public List<BorrowOrder> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectBatchIds(ids).stream().map(this::toDomain).toList();
    }


    @Override
    public List<BorrowOrder> findByReader(Long readerId, String status, int offset, int limit) {
        return mapper
                .selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                                offset / Math.max(1, limit) + 1, limit, false),
                        Wrappers.<BorrowOrderPO>lambdaQuery()
                                .eq(BorrowOrderPO::getReaderId, readerId)
                                .in(status != null && "ACTIVE".equalsIgnoreCase(status), BorrowOrderPO::getStatus, ACTIVE)
                                .eq(status != null && !"ACTIVE".equalsIgnoreCase(status), BorrowOrderPO::getStatus, status)
                                .orderByDesc(BorrowOrderPO::getCreatedAt)
                                .orderByDesc(BorrowOrderPO::getId))
                .getRecords()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByReader(Long readerId, String status) {
        return mapper.selectCount(Wrappers.<BorrowOrderPO>lambdaQuery()
                .eq(BorrowOrderPO::getReaderId, readerId)
                .in(status != null && "ACTIVE".equalsIgnoreCase(status), BorrowOrderPO::getStatus, ACTIVE)
                .eq(status != null && !"ACTIVE".equalsIgnoreCase(status), BorrowOrderPO::getStatus, status));
    }

    @Override
    public long countActive(long readerId) {
        return mapper.selectCount(Wrappers.<BorrowOrderPO>lambdaQuery()
                .eq(BorrowOrderPO::getReaderId, readerId)
                .in(BorrowOrderPO::getStatus, ACTIVE));
    }

    @Override
    public Long insert(BorrowOrder order) {
        BorrowOrderPO po = new BorrowOrderPO();
        po.setOrderNo(order.getOrderNo());
        po.setReaderId(order.getReaderId());
        po.setBookId(order.getBookId());
        po.setCopyId(order.getCopyId());
        po.setBorrowedAt(order.getBorrowedAt());
        po.setDueAt(order.getDueAt());
        po.setEffectiveDueAt(order.getEffectiveDueAt());
        po.setGraceDays(order.getGraceDays());
        po.setRenewCount(order.getRenewCount());
        po.setFinalOverdueDays(order.getFinalOverdueDays());
        po.setStatus(order.getStatus().name());
        mapper.insert(po);
        return po.getId();
    }

    @Override
    public int update(BorrowOrder order) {
        BorrowOrderPO po = new BorrowOrderPO();
        po.setId(order.getId());
        po.setDueAt(order.getDueAt());
        po.setEffectiveDueAt(order.getEffectiveDueAt());
        po.setGraceDays(order.getGraceDays());
        po.setRenewCount(order.getRenewCount());
        po.setReturnedAt(order.getReturnedAt());
        po.setFinalOverdueDays(order.getFinalOverdueDays());
        po.setStatus(order.getStatus().name());
        po.setReturnCondition(order.getReturnCondition() == null ? null : order.getReturnCondition().name());
        po.setVersion(order.getVersion());
        return mapper.updateById(po);
    }

    private BorrowOrder toDomain(BorrowOrderPO po) {
        return new BorrowOrder(
                po.getId(),
                po.getOrderNo(),
                po.getReaderId(),
                po.getBookId(),
                po.getCopyId(),
                po.getBorrowedAt(),
                po.getDueAt(),
                po.getEffectiveDueAt(),
                po.getGraceDays() == null ? 0 : po.getGraceDays(),
                po.getRenewCount() == null ? 0 : po.getRenewCount(),
                po.getReturnedAt(),
                po.getFinalOverdueDays() == null ? 0 : po.getFinalOverdueDays(),
                BorrowStatus.valueOf(po.getStatus()),
                po.getReturnCondition() == null ? null : ReturnCondition.valueOf(po.getReturnCondition()),
                po.getVersion());
    }
}
