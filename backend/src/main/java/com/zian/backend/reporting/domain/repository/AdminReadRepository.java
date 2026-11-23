package com.zian.backend.reporting.domain.repository;

import com.zian.backend.reporting.domain.model.AppealEvidenceRow;
import com.zian.backend.reporting.domain.model.AppealRow;
import com.zian.backend.reporting.domain.model.AuditLogRow;
import com.zian.backend.reporting.domain.model.CalendarRow;
import com.zian.backend.reporting.domain.model.CirculationRow;
import com.zian.backend.reporting.domain.model.DueTodayRow;
import com.zian.backend.reporting.domain.model.GraceEvidenceRow;
import com.zian.backend.reporting.domain.model.GraceRow;
import com.zian.backend.reporting.domain.model.NoticeRow;
import com.zian.backend.reporting.domain.model.NotifyTaskRow;
import com.zian.backend.reporting.domain.model.OverdueRow;
import com.zian.backend.reporting.domain.model.ReservationRow;
import com.zian.backend.reporting.domain.model.TierRow;
import com.zian.backend.reporting.domain.model.TrendRow;
import com.zian.backend.reporting.domain.model.UserRow;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端读模型端口：只读、跨表、返回扁平结果行。
 *
 * <p>读模型不建聚合，应用层只依赖本端口；SQL 由 infrastructure 下的 Mapper 提供，结果行是
 * {@code reporting.domain.model} 下的 {@code *Row} record。
 */
public interface AdminReadRepository {

    /**
     * 待审批宽限数。
     *
     * @return 待审批宽限单数量
     */
    int countPendingGraces();

    /**
     * 待处理申诉数。
     *
     * @return 待处理工单数量
     */
    int countPendingAppeals();

    /**
     * 今日到期借阅数。
     *
     * @return 今日到期借阅单数量
     */
    int countDueToday();

    /**
     * 逾期借阅数。
     *
     * @return 逾期借阅单数量
     */
    int countOverdueBooks();

    /**
     * 逾期读者数（去重）。
     *
     * @return 逾期读者数量
     */
    int countOverdueReaders();

    /**
     * 已超 SLA 未回应的工单数。
     *
     * @return SLA 违约工单数量
     */
    int countSlaBreach();

    /**
     * 所有逾期借阅的逾期天数合计，用于罚金。
     *
     * @return 逾期天数合计
     */
    long sumOverdueDays();

    /**
     * 按日统计借出量。
     *
     * @param from 统计起始时刻（含）
     * @return 每日一行的借出量
     */
    List<TrendRow> borrowTrend(LocalDateTime from);

    /**
     * 按日统计归还量。
     *
     * @param from 统计起始时刻（含）
     * @return 每日一行的归还量
     */
    List<TrendRow> returnTrend(LocalDateTime from);

    /**
     * 最近已发送的通知。
     *
     * @return 通知行
     */
    List<NoticeRow> notices();

    /**
     * 今日到期的借阅明细。
     *
     * @return 到期借阅行
     */
    List<DueTodayRow> dueTodayItems();

    /**
     * 宽限单分页查询。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 宽限单行
     */
    List<GraceRow> graces(String status, String keyword, int limit, long offset);

    /**
     * 宽限单计数。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @return 总条数
     */
    long countGraces(String status, String keyword);

    /**
     * 按主键查询宽限单（管理端详情，含读者与书目）。
     *
     * @param id 宽限申请 id
     * @return 宽限单行；不存在时返回 null
     */
    GraceRow findGrace(long id);

    /**
     * 批量查询宽限单的证据附件，用于管理端列表与详情避免 N+1。
     *
     * @param graceIds 宽限申请 id 集合，不能为空
     * @return 附件行（按关联时间升序）
     */
    List<GraceEvidenceRow> graceEvidence(List<Long> graceIds);

    /**
     * 申诉工单分页查询。
     *
     * @param status   状态过滤
     * @param category 分类过滤
     * @param keyword  关键字
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 工单行
     */
    List<AppealRow> appeals(String status, String category, String keyword, int limit, long offset);

    /**
     * 工单计数。
     *
     * @param status   状态过滤
     * @param category 分类过滤
     * @param keyword  关键字
     * @return 总条数
     */
    long countAppeals(String status, String category, String keyword);

    /**
     * 批量查询申诉工单的证据附件，用于管理端列表避免 N+1。
     *
     * @param appealIds 申诉工单 id 集合，不能为空
     * @return 附件行（按关联时间升序）
     */
    List<AppealEvidenceRow> appealEvidence(List<Long> appealIds);

    /**
     * 预约分页查询。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 预约行
     */
    List<ReservationRow> reservations(String status, String keyword, int limit, long offset);

    /**
     * 预约计数。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @return 总条数
     */
    long countReservations(String status, String keyword);

    /**
     * 逾期看板分页查询。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 逾期行
     */
    List<OverdueRow> overdue(String status, String keyword, int limit, long offset);

    /**
     * 逾期计数。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @return 总条数
     */
    long countOverdue(String status, String keyword);

    /**
     * 通知任务分页查询。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 通知任务行
     */
    List<NotifyTaskRow> notifyTasks(String status, String keyword, int limit, long offset);

    /**
     * 通知任务计数。
     *
     * @param status  状态过滤
     * @param keyword 关键字
     * @return 总条数
     */
    long countNotifyTasks(String status, String keyword);

    /**
     * 用户分页查询。
     *
     * @param role    角色过滤
     * @param status  状态过滤
     * @param keyword 关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 用户行
     */
    List<UserRow> users(String role, String status, String keyword, int limit, long offset);

    /**
     * 用户计数。
     *
     * @param role    角色过滤
     * @param status  状态过滤
     * @param keyword 关键字
     * @return 总条数
     */
    long countUsers(String role, String status, String keyword);

    /**
     * 审计日志分页查询。
     *
     * @param category 分类过滤
     * @param keyword  关键字
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 审计日志行
     */
    List<AuditLogRow> auditLogs(String category, String keyword, int limit, long offset);

    /**
     * 审计日志计数。
     *
     * @param category 分类过滤
     * @param keyword  关键字
     * @return 总条数
     */
    long countAuditLogs(String category, String keyword);

    /**
     * 审计日志详情。
     *
     * @param id 日志 ID
     * @return 审计日志行，不存在时为 null
     */
    AuditLogRow auditLog(Long id);

    /**
     * 今日借出流水。
     *
     * @return 流通流水行
     */
    List<CirculationRow> circulationBorrows();

    /**
     * 今日归还流水。
     *
     * @return 流通流水行
     */
    List<CirculationRow> circulationReturns();

    /**
     * 今日借出计数。
     *
     * @return 今日借出册数
     */
    int countBorrowsToday();

    /**
     * 今日归还计数。
     *
     * @return 今日归还册数
     */
    int countReturnsToday();

    /**
     * 逾期分档表。
     *
     * @return 分档行
     */
    List<TierRow> tiers();

    /**
     * 校历列表。
     *
     * @return 校历行
     */
    List<CalendarRow> calendar();
}
