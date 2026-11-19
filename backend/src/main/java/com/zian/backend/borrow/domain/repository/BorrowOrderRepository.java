package com.zian.backend.borrow.domain.repository;

import com.zian.backend.borrow.domain.model.BorrowOrder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 借阅单仓储。
 */
public interface BorrowOrderRepository {

    /**
     * 按 ID 查借阅单，不存在返回空。
     */

    Optional<BorrowOrder> findById(Long id);

    /**
     * 查该副本当前在借（BORROWED/OVERDUE）的单。
     */

    Optional<BorrowOrder> findActiveByCopyId(Long copyId);

    /**
     * 查读者对同一书目的在借单，用于重复借阅拦截。
     */

    Optional<BorrowOrder> findActiveByReaderAndBook(Long readerId, Long bookId);

    /**
     * 批量按 ID 查借阅单。
     */

    List<BorrowOrder> findByIds(Collection<Long> ids);

    /**
     * 按读者分页查询借阅单，status 为空表示全部。
     */
    List<BorrowOrder> findByReader(Long readerId, String status, int offset, int limit);

    /**
     * 统计读者借阅单数。
     */

    long countByReader(Long readerId, String status);

    /**
     * 统计读者当前在借（BORROWED/OVERDUE）册数，用于借阅配额校验。
     *
     * @param readerId 读者用户 ID
     * @return 在借册数
     */
    long countActive(long readerId);

    /**
     * 新建借阅单，返回生成的主键。
     */
    Long insert(BorrowOrder order);

    /**
     * 回写应还日、宽限、续借与归还结果，返回影响行数。
     */

    int update(BorrowOrder order);
}
