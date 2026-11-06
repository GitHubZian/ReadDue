package com.zian.backend.borrow.domain.repository;

import com.zian.backend.borrow.domain.model.BorrowOrder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 借阅单仓储。
 */
public interface BorrowOrderRepository {

    Optional<BorrowOrder> findById(Long id);

    Optional<BorrowOrder> findActiveByCopyId(Long copyId);

    Optional<BorrowOrder> findActiveByReaderAndBook(Long readerId, Long bookId);

    List<BorrowOrder> findByIds(Collection<Long> ids);

    /** 插入并返回生成的主键 */
    Long insert(BorrowOrder order);

    int update(BorrowOrder order);
}
