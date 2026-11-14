package com.zian.backend.reporting.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理端读模型端口：只读、跨表、返回扁平结果行。
 *
 * <p>读模型不建聚合，应用层只依赖本端口；SQL 由 infrastructure 下的 Mapper 提供。
 */
public interface AdminReadRepository {

    /**
     * 待审批宽限数。
     */

    int countPendingGraces();

    /**
     * 待处理申诉数。
     */

    int countPendingAppeals();

    /**
     * 今日到期借阅数。
     */

    int countDueToday();

    /**
     * 逾期借阅数。
     */

    int countOverdueBooks();

    /**
     * 逾期读者数（去重）。
     */

    int countOverdueReaders();

    /**
     * 已超 SLA 未回应的工单数。
     */

    int countSlaBreach();

    /**
     * 所有逾期借阅的逾期天数合计，用于罚金。
     */

    long sumOverdueDays();

    /**
     * 按日统计借出量。
     */

    List<Map<String, Object>> borrowTrend(LocalDateTime from);

    /**
     * 按日统计归还量。
     */

    List<Map<String, Object>> returnTrend(LocalDateTime from);

    /**
     * 最近已发送的通知。
     */

    List<Map<String, Object>> notices();

    /**
     * 今日到期的借阅明细。
     */

    List<Map<String, Object>> dueTodayItems();

    /**
     * 宽限单分页查询。
     */

    List<Map<String, Object>> graces(String status, String keyword, int limit, long offset);

    /**
     * 宽限单计数。
     */

    long countGraces(String status, String keyword);

    /**
     * 申诉工单分页查询。
     */

    List<Map<String, Object>> appeals(String status, String category, String keyword, int limit, long offset);

    /**
     * 工单计数。
     */

    long countAppeals(String status, String category, String keyword);

    /**
     * 预约分页查询。
     */

    List<Map<String, Object>> reservations(String status, String keyword, int limit, long offset);

    /**
     * 预约计数。
     */

    long countReservations(String status, String keyword);

    /**
     * 逾期看板分页查询。
     */

    List<Map<String, Object>> overdue(String status, String keyword, int limit, long offset);

    /**
     * 逾期计数。
     */

    long countOverdue(String status, String keyword);

    /**
     * 通知任务分页查询。
     */

    List<Map<String, Object>> notifyTasks(String status, String keyword, int limit, long offset);

    /**
     * 通知任务计数。
     */

    long countNotifyTasks(String status, String keyword);

    /**
     * 用户分页查询。
     */

    List<Map<String, Object>> users(String role, String status, String keyword, int limit, long offset);

    /**
     * 用户计数。
     */

    long countUsers(String role, String status, String keyword);

    /**
     * 审计日志分页查询。
     */

    List<Map<String, Object>> auditLogs(String category, String keyword, int limit, long offset);

    /**
     * 审计日志计数。
     */

    long countAuditLogs(String category, String keyword);

    /**
     * 审计日志详情。
     */

    Map<String, Object> auditLog(Long id);

    /**
     * 今日借出流水。
     */

    List<Map<String, Object>> circulationBorrows();

    /**
     * 今日归还流水。
     */

    List<Map<String, Object>> circulationReturns();

    /**
     * 今日借出计数。
     */

    int countBorrowsToday();

    /**
     * 今日归还计数。
     */

    int countReturnsToday();

    /**
     * 逾期分档表。
     */

    List<Map<String, Object>> tiers();

    /**
     * 校历列表。
     */

    List<Map<String, Object>> calendar();
}
