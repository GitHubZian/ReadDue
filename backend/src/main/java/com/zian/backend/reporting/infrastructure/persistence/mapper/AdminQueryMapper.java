package com.zian.backend.reporting.infrastructure.persistence.mapper;

import com.zian.backend.reporting.domain.model.AppealEvidenceRow;
import com.zian.backend.reporting.domain.model.AppealRow;
import com.zian.backend.reporting.domain.model.AuditLogRow;
import com.zian.backend.reporting.domain.model.CalendarRow;
import com.zian.backend.reporting.domain.model.CirculationRow;
import com.zian.backend.reporting.domain.model.DataReadRow;
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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理端只读查询的 MyBatis 映射。跨上下文联表与聚合集中在这里，宿主上下文不直连其他上下文的 Mapper。
 *
 * <p>由 {@code AdminReadRepositoryImpl} 适配成应用层端口。
 *
 * <p>结果列一律显式写成与 {@code *Row} 构造器参数同名的 camelCase 别名，配合
 * {@code arg-name-based-constructor-auto-mapping} 直接映射成 record，不依赖下划线转换。
 */
@Mapper
public interface AdminQueryMapper {

// region 工作台


    @Select("SELECT COUNT(*) FROM grace_application WHERE status = 'PENDING'")
    int countPendingGraces();

    @Select("SELECT COUNT(*) FROM appeal_ticket WHERE status IN ('OPEN','ACKED')")
    int countPendingAppeals();

    @Select("""
            SELECT COUNT(*) FROM borrow_order
             WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at = CURDATE()
            """)
    int countDueToday();

    @Select("""
            SELECT COUNT(*) FROM borrow_order
             WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at < CURDATE()
            """)
    int countOverdueBooks();

    @Select("""
            SELECT COUNT(DISTINCT reader_id) FROM borrow_order
             WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at < CURDATE()
            """)
    int countOverdueReaders();

    @Select("""
            SELECT COUNT(*) FROM appeal_ticket
             WHERE status IN ('OPEN','ACKED') AND sla_breached = 0 AND sla_due_at < NOW()
            """)
    int countSlaBreach();

    @Select("""
            SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), effective_due_at), 0)), 0)
              FROM borrow_order
             WHERE status IN ('BORROWED','OVERDUE') AND effective_due_at < CURDATE()
            """)
    long sumOverdueDays();

    /**
     * 按日统计借出量。
     *
     * @param from 统计起始时刻（含）
     * @return 每日一行的借出量
     */
    @Select("""
            SELECT DATE(borrowed_at) AS day, COUNT(*) AS total
              FROM borrow_order
             WHERE borrowed_at >= #{from}
             GROUP BY DATE(borrowed_at)
            """)
    List<TrendRow> borrowTrend(@Param("from") LocalDateTime from);

    /**
     * 按日统计归还量。
     *
     * @param from 统计起始时刻（含）
     * @return 每日一行的归还量
     */
    @Select("""
            SELECT DATE(returned_at) AS day, COUNT(*) AS total
              FROM borrow_order
             WHERE returned_at >= #{from}
             GROUP BY DATE(returned_at)
            """)
    List<TrendRow> returnTrend(@Param("from") LocalDateTime from);

    /**
     * 最近 5 条已发送的通知。
     *
     * @return 通知行
     */
    @Select("""
            SELECT t.id, t.level, t.plan_at AS planAt, u.name AS readerName
              FROM notify_task t
              LEFT JOIN sys_user u ON u.id = t.reader_id AND u.deleted = 0
             WHERE t.status = 'SENT'
             ORDER BY t.plan_at DESC
             LIMIT 5
            """)
    List<NoticeRow> notices();

    /**
     * 今日到期的借阅明细（最多 8 条）。
     *
     * @return 到期借阅行
     */
    @Select("""
            SELECT o.id, o.reader_id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author, c.barcode, o.effective_due_at AS effectiveDueAt
              FROM borrow_order o
              JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE o.status IN ('BORROWED','OVERDUE') AND o.effective_due_at = CURDATE()
             ORDER BY o.effective_due_at
             LIMIT 8
            """)
    List<DueTodayRow> dueTodayItems();

// endregion

// region 宽限


    /**
     * 宽限单分页查询。
     *
     * @param status  状态过滤，ALL/空表示不过滤
     * @param keyword 姓名、学号、书名或单号关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 宽限单行
     */
    @Select("""
            <script>
            SELECT g.id, g.apply_no AS applyNo, g.scene_code AS sceneCode, g.evidence_text AS evidenceText,
                   g.status, g.computed_days AS computedDays, g.granted_days AS grantedDays,
                   g.created_at AS createdAt, g.decided_at AS decidedAt, g.decided_by AS decidedBy,
                   g.reject_code AS rejectCode,
                   u.id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author, c.barcode
              FROM grace_application g
              JOIN sys_user u ON u.id = g.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN borrow_order o ON o.id = g.borrow_order_id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE 1 = 1
            <if test="status != null and status != '' and status != 'ALL'">
              <choose>
                <when test="status == 'PENDING'"> AND g.status IN ('PENDING','APPEALING')</when>
                <when test="status == 'APPROVED'"> AND g.status = 'APPROVED'</when>
                <when test="status == 'REJECTED'"> AND g.status = 'REJECTED'</when>
                <when test="status == 'EXPIRED'"> AND g.status IN ('EXPIRED','CLOSED')</when>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR b.title LIKE CONCAT('%', #{keyword}, '%')
                OR g.apply_no LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY g.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<GraceRow> graces(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM grace_application g
              JOIN sys_user u ON u.id = g.reader_id AND u.deleted = 0
              JOIN borrow_order o ON o.id = g.borrow_order_id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
             WHERE 1 = 1
            <if test="status != null and status != '' and status != 'ALL'">
              <choose>
                <when test="status == 'PENDING'"> AND g.status IN ('PENDING','APPEALING')</when>
                <when test="status == 'APPROVED'"> AND g.status = 'APPROVED'</when>
                <when test="status == 'REJECTED'"> AND g.status = 'REJECTED'</when>
                <when test="status == 'EXPIRED'"> AND g.status IN ('EXPIRED','CLOSED')</when>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR b.title LIKE CONCAT('%', #{keyword}, '%')
                OR g.apply_no LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countGraces(@Param("status") String status, @Param("keyword") String keyword);

    /**
     * 按主键查询一条宽限单（管理端详情）。
     *
     * @param id 宽限申请 id
     * @return 宽限单行；不存在时返回 null
     */
    @Select("""
            SELECT g.id, g.apply_no AS applyNo, g.scene_code AS sceneCode, g.evidence_text AS evidenceText,
                   g.status, g.computed_days AS computedDays, g.granted_days AS grantedDays,
                   g.created_at AS createdAt, g.decided_at AS decidedAt, g.decided_by AS decidedBy,
                   g.reject_code AS rejectCode,
                   u.id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author, c.barcode
              FROM grace_application g
              JOIN sys_user u ON u.id = g.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN borrow_order o ON o.id = g.borrow_order_id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE g.id = #{id}
            """)
    GraceRow findGrace(@Param("id") long id);

    /**
     * 批量查询宽限单的证据附件。
     *
     * @param graceIds 宽限申请 id 集合
     * @return 附件行
     */
    @Select("""
            <script>
            SELECT ge.grace_id AS graceId, ge.file_id AS fileId, f.original_name AS name
              FROM grace_evidence ge
              JOIN file_object f ON f.id = ge.file_id
             WHERE ge.grace_id IN
             <foreach collection="graceIds" item="gid" open="(" separator="," close=")">#{gid}</foreach>
             ORDER BY ge.created_at, ge.id
            </script>
            """)
    List<GraceEvidenceRow> graceEvidence(@Param("graceIds") List<Long> graceIds);

// endregion

// region 申诉


    /**
     * 申诉工单分页查询。
     *
     * @param status   状态过滤，ALL/空表示不过滤
     * @param category 分类过滤，ALL/空表示不过滤
     * @param keyword  姓名、学号、书名、工单号或陈述关键字
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 工单行
     */
    @Select("""
            <script>
            SELECT a.id, a.ticket_no AS ticketNo, a.target_type AS targetType, a.reason, a.status,
                   a.created_at AS createdAt, a.updated_at AS updatedAt, a.sla_due_at AS slaDueAt,
                   a.first_reply_at AS firstReplyAt, a.resolution, a.compensation_days AS compensationDays,
                   a.assignee_name AS assigneeName,
                   u.id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author, c.barcode
              FROM appeal_ticket a
              JOIN sys_user u ON u.id = a.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              LEFT JOIN borrow_order o ON o.id = CASE
                   WHEN a.target_type = 'OVERDUE_TIER' THEN a.target_id
                   WHEN a.target_type = 'GRACE' THEN (SELECT ga.borrow_order_id FROM grace_application ga WHERE ga.id = a.target_id)
                   ELSE NULL END
              LEFT JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE 1 = 1
            <if test="status != null and status != '' and status != 'ALL'">
              <choose>
                <when test="status == 'PENDING'"> AND a.status = 'OPEN'</when>
                <when test="status == 'PROCESSING'"> AND a.status = 'ACKED'</when>
                <when test="status == 'RESOLVED'"> AND a.status IN ('RESOLVED','AUTO_COMPENSATED','CLOSED')</when>
                <when test="status == 'REJECTED'"> AND a.status = 'REJECTED'</when>
              </choose>
            </if>
            <if test="category != null and category != '' and category != 'ALL'">
              <choose>
                <when test="category == 'OVERDUE_DISPUTE'"> AND a.target_type = 'OVERDUE_TIER'</when>
                <when test="category == 'FINE_DISPUTE'"> AND a.target_type = 'GRACE'</when>
                <otherwise> AND a.target_type NOT IN ('OVERDUE_TIER','GRACE')</otherwise>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR b.title LIKE CONCAT('%', #{keyword}, '%')
                OR a.ticket_no LIKE CONCAT('%', #{keyword}, '%')
                OR a.reason LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY a.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<AppealRow> appeals(
            @Param("status") String status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM appeal_ticket a
              JOIN sys_user u ON u.id = a.reader_id AND u.deleted = 0
              LEFT JOIN borrow_order o ON o.id = CASE
                   WHEN a.target_type = 'OVERDUE_TIER' THEN a.target_id
                   WHEN a.target_type = 'GRACE' THEN (SELECT ga.borrow_order_id FROM grace_application ga WHERE ga.id = a.target_id)
                   ELSE NULL END
              LEFT JOIN book b ON b.id = o.book_id AND b.deleted = 0
             WHERE 1 = 1
            <if test="status != null and status != '' and status != 'ALL'">
              <choose>
                <when test="status == 'PENDING'"> AND a.status = 'OPEN'</when>
                <when test="status == 'PROCESSING'"> AND a.status = 'ACKED'</when>
                <when test="status == 'RESOLVED'"> AND a.status IN ('RESOLVED','AUTO_COMPENSATED','CLOSED')</when>
                <when test="status == 'REJECTED'"> AND a.status = 'REJECTED'</when>
              </choose>
            </if>
            <if test="category != null and category != '' and category != 'ALL'">
              <choose>
                <when test="category == 'OVERDUE_DISPUTE'"> AND a.target_type = 'OVERDUE_TIER'</when>
                <when test="category == 'FINE_DISPUTE'"> AND a.target_type = 'GRACE'</when>
                <otherwise> AND a.target_type NOT IN ('OVERDUE_TIER','GRACE')</otherwise>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR b.title LIKE CONCAT('%', #{keyword}, '%')
                OR a.ticket_no LIKE CONCAT('%', #{keyword}, '%')
                OR a.reason LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countAppeals(
            @Param("status") String status, @Param("category") String category, @Param("keyword") String keyword);

    /**
     * 批量查询申诉工单的证据附件。
     *
     * @param appealIds 申诉工单 id 集合
     * @return 附件行
     */
    @Select("""
            <script>
            SELECT ae.appeal_id AS appealId, ae.file_id AS fileId, f.original_name AS name
              FROM appeal_evidence ae
              JOIN file_object f ON f.id = ae.file_id
             WHERE ae.appeal_id IN
             <foreach collection="appealIds" item="aid" open="(" separator="," close=")">#{aid}</foreach>
             ORDER BY ae.created_at, ae.id
            </script>
            """)
    List<AppealEvidenceRow> appealEvidence(@Param("appealIds") List<Long> appealIds);

// endregion

// region 预约


    /**
     * 预约分页查询。
     *
     * @param status  状态过滤，ALL/空表示不过滤
     * @param keyword 姓名、学号或书名关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 预约行
     */
    @Select("""
            <script>
            SELECT r.id, r.queue_no AS queueNo, r.status, r.created_at AS createdAt,
                   r.ready_at AS readyAt, r.expires_at AS expiresAt,
                   u.id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author
              FROM reservation r
              JOIN sys_user u ON u.id = r.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = r.book_id AND b.deleted = 0
             WHERE 1 = 1
            <if test="status != null and status != '' and status != 'ALL'">
              AND r.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR b.title LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY r.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ReservationRow> reservations(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM reservation r
              JOIN sys_user u ON u.id = r.reader_id AND u.deleted = 0
              JOIN book b ON b.id = r.book_id AND b.deleted = 0
             WHERE 1 = 1
            <if test="status != null and status != '' and status != 'ALL'">
              AND r.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR b.title LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countReservations(@Param("status") String status, @Param("keyword") String keyword);

// endregion

// region 逾期


    /**
     * 逾期看板分页查询。
     *
     * @param status  状态过滤，ALL/空表示未丢书的逾期
     * @param keyword 姓名、学号或书名关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 逾期行
     */
    @Select("""
            <script>
            SELECT v.order_id AS orderId, v.reader_id AS readerId, v.reader_name AS readerName,
                   v.student_no AS studentNo, v.book_id AS bookId, v.book_title AS bookTitle,
                   v.effective_due_at AS effectiveDueAt, v.overdue_days AS overdueDays, v.l3_sent AS l3Sent,
                   o.status,
                   (SELECT MAX(t.created_at) FROM notify_task t
                     WHERE t.borrow_order_id = v.order_id AND t.level = 'L3') AS lastReminded,
                   (SELECT p.department FROM reader_profile p WHERE p.user_id = v.reader_id) AS department,
                   c.barcode, (SELECT b.author FROM book b WHERE b.id = v.book_id AND b.deleted = 0) AS author
              FROM v_overdue_board v
              JOIN borrow_order o ON o.id = v.order_id
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE v.effective_due_at &lt; CURDATE()
            <if test="status != null and status != '' and status != 'ALL'">
              <choose>
                <when test="status == 'LOST'"> AND o.status = 'LOST'</when>
                <otherwise> AND o.status IN ('BORROWED','OVERDUE')</otherwise>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (v.reader_name LIKE CONCAT('%', #{keyword}, '%')
                OR v.student_no LIKE CONCAT('%', #{keyword}, '%')
                OR v.book_title LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY v.overdue_days DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<OverdueRow> overdue(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM v_overdue_board v
              JOIN borrow_order o ON o.id = v.order_id
             WHERE v.effective_due_at &lt; CURDATE()
            <if test="status != null and status != '' and status != 'ALL'">
              <choose>
                <when test="status == 'LOST'"> AND o.status = 'LOST'</when>
                <otherwise> AND o.status IN ('BORROWED','OVERDUE')</otherwise>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (v.reader_name LIKE CONCAT('%', #{keyword}, '%')
                OR v.student_no LIKE CONCAT('%', #{keyword}, '%')
                OR v.book_title LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countOverdue(@Param("status") String status, @Param("keyword") String keyword);

// endregion

// region 提醒任务


    /**
     * 通知任务分页查询。
     *
     * @param status  状态过滤，PLANNED 会展开为 PLANNED/CANCELED/MERGED
     * @param keyword 读者姓名或去重键关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 通知任务行
     */
    @Select("""
            <script>
            SELECT t.id, t.level, t.status, t.created_at AS createdAt, t.plan_at AS planAt,
                   t.updated_at AS updatedAt, t.dedup_key AS dedupKey, u.name AS readerName
              FROM notify_task t
              LEFT JOIN sys_user u ON u.id = t.reader_id AND u.deleted = 0
             WHERE 1 = 1
            <choose>
              <when test="status != null and status != '' and status != 'ALL' and status == 'PLANNED'">
                AND t.status IN ('PLANNED', 'CANCELED', 'MERGED')
              </when>
              <when test="status != null and status != '' and status != 'ALL'">
                AND t.status = #{status}
              </when>
            </choose>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR t.dedup_key LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY t.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<NotifyTaskRow> notifyTasks(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM notify_task t
              LEFT JOIN sys_user u ON u.id = t.reader_id AND u.deleted = 0
             WHERE 1 = 1
            <choose>
              <when test="status != null and status != '' and status != 'ALL' and status == 'PLANNED'">
                AND t.status IN ('PLANNED', 'CANCELED', 'MERGED')
              </when>
              <when test="status != null and status != '' and status != 'ALL'">
                AND t.status = #{status}
              </when>
            </choose>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR t.dedup_key LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countNotifyTasks(@Param("status") String status, @Param("keyword") String keyword);

// endregion

// region 用户


    /**
     * 用户分页查询。
     *
     * @param role    角色过滤，ALL/空表示不过滤
     * @param status  状态过滤，ALL/空表示不过滤
     * @param keyword 姓名、学号或手机号关键字
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 用户行
     */
    @Select("""
            <script>
            SELECT u.id, u.name, u.user_no AS userNo, u.role, u.status, u.phone, u.email,
                   u.created_at AS createdAt, u.last_login_at AS lastLoginAt,
                   u.blacklist_reason AS blacklistReason, rp.department,
                   (SELECT COUNT(*) FROM borrow_order o WHERE o.reader_id = u.id) AS borrowCount,
                   (SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), o.effective_due_at), 0)), 0)
                      FROM borrow_order o
                     WHERE o.reader_id = u.id AND o.status IN ('BORROWED','OVERDUE')) AS overdueDays
              FROM sys_user u
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
             WHERE u.deleted = 0
            <if test="role != null and role != '' and role != 'ALL'">
              AND u.role = #{role}
            </if>
            <if test="status != null and status != '' and status != 'ALL'">
              AND u.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (
                (#{keyword} REGEXP '^1[0-9]{10}$' AND u.phone = #{keyword})
                OR (#{keyword} NOT REGEXP '^1[0-9]{10}$'
                    AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                      OR u.user_no LIKE CONCAT('%', #{keyword}, '%')))
              )
            </if>
             ORDER BY u.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<UserRow> users(
            @Param("role") String role,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM sys_user u
             WHERE u.deleted = 0
            <if test="role != null and role != '' and role != 'ALL'">
              AND u.role = #{role}
            </if>
            <if test="status != null and status != '' and status != 'ALL'">
              AND u.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (
                (#{keyword} REGEXP '^1[0-9]{10}$' AND u.phone = #{keyword})
                OR (#{keyword} NOT REGEXP '^1[0-9]{10}$'
                    AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                      OR u.user_no LIKE CONCAT('%', #{keyword}, '%')))
              )
            </if>
            </script>
            """)
    long countUsers(
            @Param("role") String role, @Param("status") String status, @Param("keyword") String keyword);

    /**
     * 按主键查用户详情（管理端敏感读取，需另记 DATA_READ 审计）。
     *
     * @param id 用户主键
     * @return 用户行，不存在时为 null
     */
    @Select("""
            SELECT u.id, u.name, u.user_no AS userNo, u.role, u.status, u.phone, u.email,
                   u.created_at AS createdAt, u.last_login_at AS lastLoginAt,
                   u.blacklist_reason AS blacklistReason, rp.department,
                   (SELECT COUNT(*) FROM borrow_order o WHERE o.reader_id = u.id) AS borrowCount,
                   (SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), o.effective_due_at), 0)), 0)
                      FROM borrow_order o
                     WHERE o.reader_id = u.id AND o.status IN ('BORROWED','OVERDUE')) AS overdueDays
              FROM sys_user u
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
             WHERE u.deleted = 0 AND u.id = #{id}
            """)
    UserRow user(@Param("id") long id);

// endregion

// region 审计


    /**
     * 审计日志分页查询。
     *
     * @param category 分类过滤：APPROVAL/RULE/USER/SYSTEM，ALL/空表示不过滤；按动作前缀族映射
     * @param keyword  操作人、动作、对象类型或对象 ID 关键字
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 审计日志行
     */
    @Select("""
            <script>
            SELECT a.id, a.action, a.object_type AS objectType, a.object_id AS objectId, a.purpose,
                   a.detail, a.ip, a.trace_id AS traceId,
                   a.before_json AS beforeJson, a.after_json AS afterJson,
                   a.actor_role AS actorRole, a.created_at AS createdAt,
                   u.name AS actorName
              FROM audit_log a
              LEFT JOIN sys_user u ON u.id = a.actor_id
             WHERE 1 = 1
            <if test="category != null and category != '' and category != 'ALL'">
              <choose>
                <when test="category == 'APPROVAL'">
                  AND (a.action LIKE 'GRACE%' OR a.action LIKE 'APPEAL%' OR a.action LIKE 'RESERVATION%')
                </when>
                <when test="category == 'RULE'">
                  AND (a.action LIKE 'RULE%' OR a.action LIKE 'NOTIFY_TEMPLATE%')
                </when>
                <when test="category == 'USER'">
                  AND (a.action LIKE 'USER%' OR a.action LIKE 'AUTH%')
                </when>
                <when test="category == 'SYSTEM'">
                  AND NOT (a.action LIKE 'GRACE%' OR a.action LIKE 'APPEAL%' OR a.action LIKE 'RESERVATION%'
                        OR a.action LIKE 'RULE%' OR a.action LIKE 'NOTIFY_TEMPLATE%'
                        OR a.action LIKE 'USER%' OR a.action LIKE 'AUTH%')
                </when>
                <otherwise>
                  AND a.action LIKE CONCAT(#{category}, '%')
                </otherwise>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR a.action LIKE CONCAT('%', #{keyword}, '%')
                OR a.object_type LIKE CONCAT('%', #{keyword}, '%')
                OR CAST(a.object_id AS CHAR) LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY a.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<AuditLogRow> auditLogs(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*)
              FROM audit_log a
              LEFT JOIN sys_user u ON u.id = a.actor_id
             WHERE 1 = 1
            <if test="category != null and category != '' and category != 'ALL'">
              <choose>
                <when test="category == 'APPROVAL'">
                  AND (a.action LIKE 'GRACE%' OR a.action LIKE 'APPEAL%' OR a.action LIKE 'RESERVATION%')
                </when>
                <when test="category == 'RULE'">
                  AND (a.action LIKE 'RULE%' OR a.action LIKE 'NOTIFY_TEMPLATE%')
                </when>
                <when test="category == 'USER'">
                  AND (a.action LIKE 'USER%' OR a.action LIKE 'AUTH%')
                </when>
                <when test="category == 'SYSTEM'">
                  AND NOT (a.action LIKE 'GRACE%' OR a.action LIKE 'APPEAL%' OR a.action LIKE 'RESERVATION%'
                        OR a.action LIKE 'RULE%' OR a.action LIKE 'NOTIFY_TEMPLATE%'
                        OR a.action LIKE 'USER%' OR a.action LIKE 'AUTH%')
                </when>
                <otherwise>
                  AND a.action LIKE CONCAT(#{category}, '%')
                </otherwise>
              </choose>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR a.action LIKE CONCAT('%', #{keyword}, '%')
                OR a.object_type LIKE CONCAT('%', #{keyword}, '%')
                OR CAST(a.object_id AS CHAR) LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countAuditLogs(@Param("category") String category, @Param("keyword") String keyword);

    /**
     * 按主键查审计日志详情。
     *
     * @param id 日志 ID
     * @return 审计日志行，不存在时为 null
     */
    @Select("""
            SELECT a.id, a.action, a.object_type AS objectType, a.object_id AS objectId, a.purpose,
                   a.detail, a.ip, a.trace_id AS traceId,
                   a.before_json AS beforeJson, a.after_json AS afterJson,
                   a.actor_role AS actorRole, a.created_at AS createdAt,
                   u.name AS actorName
              FROM audit_log a
              LEFT JOIN sys_user u ON u.id = a.actor_id
             WHERE a.id = #{id}
            """)
    AuditLogRow auditLog(@Param("id") Long id);

    /**
     * 敏感数据读取记录（DATA_READ），按目标类型/ID 过滤，时间倒序。
     *
     * @param targetType 目标类型 USER/GRACE，空表示不过滤
     * @param targetId   目标 ID，null 表示不过滤
     * @param limit      条数上限
     * @return 读取记录
     */
    @Select("""
            <script>
            SELECT a.id, a.actor_id AS operatorId, u.name AS operatorName,
                   a.object_type AS targetType, a.object_id AS targetId, a.created_at AS createdAt
              FROM audit_log a
              LEFT JOIN sys_user u ON u.id = a.actor_id
             WHERE a.action = 'DATA_READ'
            <if test="targetType != null and targetType != ''">
              AND a.object_type = #{targetType}
            </if>
            <if test="targetId != null">
              AND a.object_id = #{targetId}
            </if>
             ORDER BY a.created_at DESC
             LIMIT #{limit}
            </script>
            """)
    List<DataReadRow> dataReads(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("limit") int limit);

// endregion

// region 借还台


    /**
     * 今日借出流水。
     *
     * @return 流通流水行
     */
    @Select("""
            SELECT o.id, o.borrowed_at AS at, 'BORROW' AS type,
                   u.id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author, c.barcode
              FROM borrow_order o
              JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE DATE(o.borrowed_at) = CURDATE()
            """)
    List<CirculationRow> circulationBorrows();

    /**
     * 今日归还流水。
     *
     * @return 流通流水行
     */
    @Select("""
            SELECT o.id, o.returned_at AS at, 'RETURN' AS type,
                   u.id AS readerId, u.name AS readerName, u.user_no AS userNo, rp.department,
                   b.id AS bookId, b.title, b.author, c.barcode
              FROM borrow_order o
              JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE DATE(o.returned_at) = CURDATE()
            """)
    List<CirculationRow> circulationReturns();

    @Select("SELECT COUNT(*) FROM borrow_order WHERE DATE(borrowed_at) = CURDATE()")
    int countBorrowsToday();

    @Select("SELECT COUNT(*) FROM borrow_order WHERE DATE(returned_at) = CURDATE()")
    int countReturnsToday();

// endregion

// region 规则附表


    /**
     * 逾期分档表。
     *
     * @return 分档行
     */
    @Select("""
            SELECT tier_code AS code, tier_name AS name, min_days AS minDays,
                   max_days AS maxDays, action_code AS actionCode, tone_code AS toneCode,
                   allow_renew AS allowRenew, description
              FROM overdue_tier
             ORDER BY min_days
            """)
    List<TierRow> tiers();

    /**
     * 校历列表。
     *
     * @return 校历行
     */
    @Select("""
            SELECT id, term, scene_code AS scene, start_date AS startDate, end_date AS endDate, source, note
              FROM academic_calendar
             WHERE deleted = 0
             ORDER BY start_date DESC
            """)
    List<CalendarRow> calendar();

// endregion

}
