package com.zian.backend.reporting.infrastructure.persistence.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理端只读查询的 MyBatis 映射。跨上下文联表与聚合集中在这里，宿主上下文不直连其他上下文的 Mapper。
 *
 * <p>由 {@code AdminReadRepositoryImpl} 适配成应用层端口。
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

    @Select("""
            SELECT DATE(borrowed_at) AS d, COUNT(*) AS c
              FROM borrow_order
             WHERE borrowed_at >= #{from}
             GROUP BY DATE(borrowed_at)
            """)
    List<Map<String, Object>> borrowTrend(@Param("from") LocalDateTime from);

    @Select("""
            SELECT DATE(returned_at) AS d, COUNT(*) AS c
              FROM borrow_order
             WHERE returned_at >= #{from}
             GROUP BY DATE(returned_at)
            """)
    List<Map<String, Object>> returnTrend(@Param("from") LocalDateTime from);

    @Select("""
            SELECT t.id, t.level, t.plan_at, u.name AS reader_name
              FROM notify_task t
              LEFT JOIN sys_user u ON u.id = t.reader_id AND u.deleted = 0
             WHERE t.status = 'SENT'
             ORDER BY t.plan_at DESC
             LIMIT 5
            """)
    List<Map<String, Object>> notices();

    @Select("""
            SELECT o.id, o.reader_id, u.name AS reader_name, u.user_no, rp.department,
                   b.id AS book_id, b.title, b.author, c.barcode, o.effective_due_at
              FROM borrow_order o
              JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE o.status IN ('BORROWED','OVERDUE') AND o.effective_due_at = CURDATE()
             ORDER BY o.effective_due_at
             LIMIT 8
            """)
    List<Map<String, Object>> dueTodayItems();

// endregion

// region 宽限


    @Select("""
            <script>
            SELECT g.id, g.apply_no, g.scene_code, g.evidence_text, g.status, g.computed_days,
                   g.granted_days, g.created_at, g.decided_at, g.decided_by, g.reject_code,
                   u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                   b.id AS book_id, b.title, b.author, c.barcode
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
    List<Map<String, Object>> graces(
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

// endregion

// region 申诉


    @Select("""
            <script>
            SELECT a.id, a.ticket_no, a.target_type, a.reason, a.status, a.created_at, a.updated_at,
                   a.sla_due_at, a.first_reply_at, a.resolution, a.compensation_days, a.assignee_name,
                   u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                   b.id AS book_id, b.title, b.author, c.barcode
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
    List<Map<String, Object>> appeals(
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

// endregion

// region 预约


    @Select("""
            <script>
            SELECT r.id, r.queue_no, r.status, r.created_at, r.ready_at, r.expires_at,
                   u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                   b.id AS book_id, b.title, b.author
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
    List<Map<String, Object>> reservations(
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


    @Select("""
            <script>
            SELECT v.order_id, v.reader_id, v.reader_name, v.student_no, v.book_id, v.book_title,
                   v.effective_due_at, v.overdue_days, v.l3_sent, o.status,
                   (SELECT MAX(t.created_at) FROM notify_task t
                     WHERE t.borrow_order_id = v.order_id AND t.level = 'L3') AS last_reminded,
                   (SELECT p.department FROM reader_profile p WHERE p.user_id = v.reader_id) AS department,
                   c.barcode, (SELECT b.author FROM book b WHERE b.id = v.book_id) AS author
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
    List<Map<String, Object>> overdue(
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


    @Select("""
            <script>
            SELECT t.id, t.level, t.status, t.created_at, t.plan_at, t.updated_at, t.dedup_key,
                   u.name AS reader_name
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
    List<Map<String, Object>> notifyTasks(
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


    @Select("""
            <script>
            SELECT u.id, u.name, u.user_no, u.role, u.status, u.phone, u.email, u.created_at, u.last_login_at,
                   u.blacklist_reason, rp.department,
                   (SELECT COUNT(*) FROM borrow_order o WHERE o.reader_id = u.id) AS borrow_count,
                   (SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), o.effective_due_at), 0)), 0)
                      FROM borrow_order o
                     WHERE o.reader_id = u.id AND o.status IN ('BORROWED','OVERDUE')) AS overdue_days
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
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR u.phone LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY u.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> users(
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
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR u.user_no LIKE CONCAT('%', #{keyword}, '%')
                OR u.phone LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countUsers(
            @Param("role") String role, @Param("status") String status, @Param("keyword") String keyword);

// endregion

// region 审计


    @Select("""
            <script>
            SELECT a.id, a.action, a.object_type, a.object_id, a.purpose, a.detail, a.ip, a.actor_role,
                   a.created_at, u.name AS actor_name
              FROM audit_log a
              LEFT JOIN sys_user u ON u.id = a.actor_id
             WHERE 1 = 1
            <if test="category != null and category != '' and category != 'ALL'">
              AND a.action LIKE CONCAT(#{category}, '%')
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR a.action LIKE CONCAT('%', #{keyword}, '%')
                OR a.object_type LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY a.created_at DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> auditLogs(
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
              AND a.action LIKE CONCAT(#{category}, '%')
            </if>
            <if test="keyword != null and keyword != ''">
              AND (u.name LIKE CONCAT('%', #{keyword}, '%')
                OR a.action LIKE CONCAT('%', #{keyword}, '%')
                OR a.object_type LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countAuditLogs(@Param("category") String category, @Param("keyword") String keyword);

    @Select("""
            SELECT a.id, a.action, a.object_type, a.object_id, a.purpose, a.detail, a.ip, a.actor_role,
                   a.created_at, u.name AS actor_name
              FROM audit_log a
              LEFT JOIN sys_user u ON u.id = a.actor_id
             WHERE a.id = #{id}
            """)
    Map<String, Object> auditLog(@Param("id") Long id);

// endregion

// region 借还台


    @Select("""
            SELECT o.id, o.borrowed_at AS at, 'BORROW' AS type,
                   u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                   b.id AS book_id, b.title, b.author, c.barcode
              FROM borrow_order o
              JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE DATE(o.borrowed_at) = CURDATE()
            """)
    List<Map<String, Object>> circulationBorrows();

    @Select("""
            SELECT o.id, o.returned_at AS at, 'RETURN' AS type,
                   u.id AS reader_id, u.name AS reader_name, u.user_no, rp.department,
                   b.id AS book_id, b.title, b.author, c.barcode
              FROM borrow_order o
              JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
              LEFT JOIN reader_profile rp ON rp.user_id = u.id
              JOIN book b ON b.id = o.book_id AND b.deleted = 0
              LEFT JOIN book_copy c ON c.id = o.copy_id AND c.deleted = 0
             WHERE DATE(o.returned_at) = CURDATE()
            """)
    List<Map<String, Object>> circulationReturns();

    @Select("SELECT COUNT(*) FROM borrow_order WHERE DATE(borrowed_at) = CURDATE()")
    int countBorrowsToday();

    @Select("SELECT COUNT(*) FROM borrow_order WHERE DATE(returned_at) = CURDATE()")
    int countReturnsToday();

// endregion

// region 规则附表


    @Select("""
            SELECT tier_code AS code, tier_name AS name, min_days AS minDays,
                   max_days AS maxDays, action_code AS actionCode, tone_code AS toneCode,
                   allow_renew AS allowRenew, description
              FROM overdue_tier
             ORDER BY min_days
            """)
    List<Map<String, Object>> tiers();

    @Select("""
            SELECT id, term, scene_code AS scene, start_date AS startDate, end_date AS endDate, source, note
              FROM academic_calendar
             WHERE deleted = 0
             ORDER BY start_date DESC
            """)
    List<Map<String, Object>> calendar();

// endregion

}
