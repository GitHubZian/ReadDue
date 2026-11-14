package com.zian.backend.appeal.domain.repository;

import com.zian.backend.appeal.domain.model.AppealTicket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 申诉工单仓储。
 */
public interface AppealTicketRepository {

    /**
     * 按 ID 查工单，不存在返回空。
     */

    Optional<AppealTicket> findById(Long id);

    /**
     * 查询已超 SLA 且尚未补偿的开放工单，按截止时间升序。
     */
    List<AppealTicket> findSlaDue(LocalDateTime now, int limit);

    /**
     * 回写状态、处理人、补偿与结论，返回影响行数。
     */

    int update(AppealTicket ticket);

    /**
     * 新建工单，返回生成的主键。
     */

    Long insert(AppealTicket ticket);

    /**
     * 按读者分页查询工单。
     */

    List<AppealTicket> findByReader(Long readerId, String status, int offset, int limit);

    /**
     * 统计读者的工单数。
     */

    long countByReader(Long readerId, String status);
}
