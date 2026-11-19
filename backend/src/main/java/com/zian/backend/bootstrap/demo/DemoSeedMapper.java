package com.zian.backend.bootstrap.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 本地演示种子数据的写入口。
 *
 * <p>只被 {@link DemoDataInitializer}（{@code @Profile("local")}）调用；Mapper Bean 本身不按 profile 隔离，
 * 因此任何业务代码都不允许依赖它，避免把「可清空业务表」的能力带进生产路径。</p>
 */
@Mapper
public interface DemoSeedMapper {

    @Select("SELECT COUNT(*) FROM sys_user")
    int countUsers();

    // region 清空演示数据（逐表显式 DELETE，不用 ${table} 拼表名；顺序满足外键依赖）

    @Delete("DELETE FROM event_outbox")
    int clearEventOutbox();

    @Delete("DELETE FROM audit_log")
    int clearAuditLog();

    @Delete("DELETE FROM notify_delivery")
    int clearNotifyDelivery();

    @Delete("DELETE FROM notify_task")
    int clearNotifyTask();

    @Delete("DELETE FROM appeal_event")
    int clearAppealEvent();

    @Delete("DELETE FROM appeal_ticket")
    int clearAppealTicket();

    @Delete("DELETE FROM lost_book_report")
    int clearLostBookReport();

    @Delete("DELETE FROM grace_application")
    int clearGraceApplication();

    @Delete("DELETE FROM reservation")
    int clearReservation();

    @Delete("DELETE FROM renew_record")
    int clearRenewRecord();

    @Delete("DELETE FROM borrow_order")
    int clearBorrowOrder();

    @Delete("DELETE FROM book_copy")
    int clearBookCopy();

    @Delete("DELETE FROM book")
    int clearBook();

    @Delete("DELETE FROM user_status_log")
    int clearUserStatusLog();

    @Delete("DELETE FROM reader_credit")
    int clearReaderCredit();

    @Delete("DELETE FROM reader_quiet_hours")
    int clearReaderQuietHours();

    @Delete("DELETE FROM reader_channel_pref")
    int clearReaderChannelPref();

    @Delete("DELETE FROM reader_profile")
    int clearReaderProfile();

    @Delete("DELETE FROM sys_user")
    int clearSysUser();

    // endregion

    @Insert("""
            INSERT INTO sys_user (id, user_no, role, name, phone, email, password_hash, status, blacklist_reason,
                                  blacklisted_at, created_at, updated_at)
            VALUES (#{id}, #{userNo}, #{role}, #{name}, #{phone}, #{email}, #{hash}, #{status}, #{reason},
                    #{blacklistedAt}, #{now}, #{now})
            """)
    int insertUser(
            @Param("id") long id,
            @Param("userNo") String userNo,
            @Param("role") String role,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("hash") String hash,
            @Param("status") String status,
            @Param("reason") String reason,
            @Param("blacklistedAt") LocalDateTime blacklistedAt,
            @Param("now") LocalDateTime now);

    @Select("SELECT id FROM sys_user WHERE user_no = #{userNo}")
    Long findUserId(@Param("userNo") String userNo);

    @Insert("INSERT INTO reader_profile (user_id, department, major, grade) VALUES (#{userId}, #{department}, #{major}, #{grade})")
    int insertProfile(
            @Param("userId") Long userId,
            @Param("department") String department,
            @Param("major") String major,
            @Param("grade") String grade);

    @Insert("""
            INSERT INTO book (id, isbn13, title, author, publisher, publish_date, category_code, language, price,
                              summary, tags, status, created_at, updated_at)
            VALUES (#{id}, #{isbn}, #{title}, #{author}, #{publisher}, #{publishDate}, #{category}, 'zh', #{price},
                    #{summary}, #{tags}, 1, NOW(), NOW())
            """)
    int insertBook(
            @Param("id") long id,
            @Param("isbn") String isbn,
            @Param("title") String title,
            @Param("author") String author,
            @Param("publisher") String publisher,
            @Param("publishDate") LocalDate publishDate,
            @Param("category") String category,
            @Param("price") BigDecimal price,
            @Param("summary") String summary,
            @Param("tags") String tags);

    @Select("SELECT id FROM book WHERE isbn13 = #{isbn}")
    Long findBookId(@Param("isbn") String isbn);

    @Insert("""
            INSERT INTO book_copy (id, book_id, barcode, location, status, acquired_at, updated_at)
            VALUES (#{id}, #{bookId}, #{barcode}, #{location}, #{status}, #{acquiredAt}, NOW())
            """)
    int insertCopy(
            @Param("id") long id,
            @Param("bookId") Long bookId,
            @Param("barcode") String barcode,
            @Param("location") String location,
            @Param("status") String status,
            @Param("acquiredAt") LocalDate acquiredAt);

    @Select("SELECT id FROM book_copy WHERE barcode = #{barcode}")
    Long findCopyId(@Param("barcode") String barcode);

    @Insert("""
            INSERT INTO borrow_order (id, order_no, reader_id, book_id, copy_id, borrowed_at, due_at, effective_due_at,
                                      grace_days, renew_count, returned_at, final_overdue_days, status, return_condition,
                                      created_at, updated_at)
            VALUES (#{id}, #{orderNo}, #{readerId}, #{bookId}, #{copyId}, #{borrowedAt}, #{dueAt}, #{effectiveDueAt},
                    #{graceDays}, #{renewCount}, #{returnedAt}, #{finalOverdueDays}, #{status}, #{returnCondition},
                    #{borrowedAt}, #{borrowedAt})
            """)
    int insertBorrow(
            @Param("id") long id,
            @Param("orderNo") String orderNo,
            @Param("readerId") Long readerId,
            @Param("bookId") Long bookId,
            @Param("copyId") Long copyId,
            @Param("borrowedAt") LocalDateTime borrowedAt,
            @Param("dueAt") LocalDate dueAt,
            @Param("effectiveDueAt") LocalDate effectiveDueAt,
            @Param("graceDays") int graceDays,
            @Param("renewCount") int renewCount,
            @Param("returnedAt") LocalDateTime returnedAt,
            @Param("finalOverdueDays") int finalOverdueDays,
            @Param("status") String status,
            @Param("returnCondition") String returnCondition);

    @Select("SELECT id FROM borrow_order WHERE order_no = #{orderNo}")
    Long findBorrowId(@Param("orderNo") String orderNo);

    @Select("SELECT id FROM grace_application WHERE apply_no = #{applyNo}")
    Long findGraceId(@Param("applyNo") String applyNo);

    @Select("SELECT id FROM appeal_ticket WHERE ticket_no = #{ticketNo}")
    Long findAppealId(@Param("ticketNo") String ticketNo);

    @Update("""
            <script>
            UPDATE book_copy SET status = #{status} WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    int updateCopyStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    @Insert("""
            INSERT INTO reservation (id, book_id, reader_id, queue_no, status, ready_at, expires_at, created_at, updated_at)
            VALUES (#{id}, #{bookId}, #{readerId}, #{queueNo}, #{status}, #{readyAt}, #{expiresAt}, #{createdAt}, #{createdAt})
            """)
    int insertReservation(
            @Param("id") long id,
            @Param("bookId") Long bookId,
            @Param("readerId") Long readerId,
            @Param("queueNo") int queueNo,
            @Param("status") String status,
            @Param("readyAt") LocalDateTime readyAt,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("createdAt") LocalDateTime createdAt);

    @Insert("""
            INSERT INTO grace_application (id, apply_no, borrow_order_id, reader_id, scene_code, scene_source, calendar_hit,
                                           base_days, kt, kq, dmax, computed_days, granted_days, evidence_text,
                                           formula_version, queue_len, snapshot_expires_at, status, decided_at,
                                           created_at, updated_at)
            VALUES (#{id}, #{applyNo}, #{orderId}, #{readerId}, #{scene}, #{source}, #{calendarHit},
                    #{baseDays}, #{kt}, #{kq}, 14, #{computedDays}, #{grantedDays}, #{evidence},
                    'v1', 2, #{snapshotExpiresAt}, #{status}, #{decidedAt}, #{createdAt}, #{createdAt})
            """)
    int insertGrace(
            @Param("id") long id,
            @Param("applyNo") String applyNo,
            @Param("orderId") Long orderId,
            @Param("readerId") Long readerId,
            @Param("scene") String scene,
            @Param("source") String source,
            @Param("calendarHit") int calendarHit,
            @Param("baseDays") BigDecimal baseDays,
            @Param("kt") BigDecimal kt,
            @Param("kq") BigDecimal kq,
            @Param("computedDays") BigDecimal computedDays,
            @Param("grantedDays") Integer grantedDays,
            @Param("evidence") String evidence,
            @Param("snapshotExpiresAt") LocalDateTime snapshotExpiresAt,
            @Param("status") String status,
            @Param("decidedAt") LocalDateTime decidedAt,
            @Param("createdAt") LocalDateTime createdAt);

    @Insert("""
            INSERT INTO appeal_ticket (id, ticket_no, reader_id, target_type, target_id, reason, status, sla_due_at,
                                       first_reply_at, sla_breached, assignee_id, assignee_name, compensation_days,
                                       resolution, created_at, updated_at)
            VALUES (#{id}, #{ticketNo}, #{readerId}, #{targetType}, #{targetId}, #{reason}, #{status}, #{slaDueAt},
                    #{firstReplyAt}, #{slaBreached}, #{assigneeId}, #{assigneeName}, 0,
                    NULL, #{createdAt}, #{createdAt})
            """)
    int insertAppeal(
            @Param("id") long id,
            @Param("ticketNo") String ticketNo,
            @Param("readerId") Long readerId,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("reason") String reason,
            @Param("status") String status,
            @Param("slaDueAt") LocalDateTime slaDueAt,
            @Param("firstReplyAt") LocalDateTime firstReplyAt,
            @Param("slaBreached") int slaBreached,
            @Param("assigneeId") Long assigneeId,
            @Param("assigneeName") String assigneeName,
            @Param("createdAt") LocalDateTime createdAt);

    @Insert("""
            INSERT INTO notify_task (id, dedup_key, reader_id, borrow_order_id, level, plan_at, quiet_shifted, status,
                                     payload, created_at, updated_at)
            VALUES (#{id}, #{dedupKey}, #{readerId}, #{orderId}, #{level}, #{planAt}, 0, #{status}, #{payload},
                    #{planAt}, #{planAt})
            """)
    int insertTask(
            @Param("id") long id,
            @Param("dedupKey") String dedupKey,
            @Param("readerId") Long readerId,
            @Param("orderId") Long orderId,
            @Param("level") String level,
            @Param("planAt") LocalDateTime planAt,
            @Param("status") String status,
            @Param("payload") String payload);

    @Insert("""
            INSERT INTO audit_log (id, actor_id, actor_role, action, object_type, object_id, purpose, detail, ip, created_at)
            VALUES (#{id}, #{actorId}, #{actorRole}, #{action}, #{objectType}, #{objectId}, #{purpose}, #{detail}, #{ip}, #{at})
            """)
    int insertAudit(
            @Param("id") long id,
            @Param("actorId") Long actorId,
            @Param("actorRole") String actorRole,
            @Param("action") String action,
            @Param("objectType") String objectType,
            @Param("objectId") Long objectId,
            @Param("purpose") String purpose,
            @Param("detail") String detail,
            @Param("ip") String ip,
            @Param("at") LocalDateTime at);
}
