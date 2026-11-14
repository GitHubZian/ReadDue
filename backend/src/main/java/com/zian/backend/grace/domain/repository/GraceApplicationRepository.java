package com.zian.backend.grace.domain.repository;

import com.zian.backend.grace.domain.model.GraceApplication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 宽限申请仓储。
 */
public interface GraceApplicationRepository {

    /**
     * 按 ID 查宽限申请。
     */

    Optional<GraceApplication> findById(Long id);

    /**
     * 回写审批结果，返回影响行数。
     */

    int update(GraceApplication application);

    /**
     * 新建宽限申请，返回主键。
     */

    Long insert(GraceApplication application);

    /**
     * 按读者分页查询申请。
     */

    List<GraceApplication> findByReader(Long readerId, String status, int offset, int limit);

    /**
     * 统计读者的申请数。
     */

    long countByReader(Long readerId, String status);

    /**
     * 统计某借阅单在指定时间后已批准的次数。
     */
    int countApprovedSince(Long borrowOrderId, LocalDateTime since);

    /**
     * 判断该借阅单是否已有待审批申请。
     */

    boolean existsPending(Long borrowOrderId);
}
