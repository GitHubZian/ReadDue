package com.zian.backend.bootstrap.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 本地演示数据：仅在 local profile 且 app.demo.enabled=true 时执行，且库中没有任何用户时插入一次。
 *
 * <p>日期全部相对 now() 计算，保证看板里的「今日到期 / 逾期」永远有数据。
 */
@Component
@Profile("local")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_PASSWORD = "123456";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final boolean reset;

    public DemoDataInitializer(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            @org.springframework.beans.factory.annotation.Value("${app.demo.reset:false}") boolean reset) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.reset = reset;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (reset) {
            clearDemoData();
        }
        Integer users = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
        if (users != null && users > 0) {
            log.info("演示数据已存在，跳过初始化");
            return;
        }
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        LocalDateTime now = LocalDateTime.now();

        long adminId = user("ADMIN001", "ADMIN", "王敏", "13800000001", "admin@readdue.edu", hash, "NORMAL", null, now);
        long librarianId =
                user("LIB001", "LIBRARIAN", "李倩", "13800000002", "liqian@readdue.edu", hash, "NORMAL", null, now);
        long chen = user("2021001", "READER", "陈昊", "13800000011", "chenhao@stu.edu", hash, "NORMAL", null, now);
        long zhou = user("2021002", "READER", "周雨", "13800000012", "zhouyu@stu.edu", hash, "NORMAL", null, now);
        long lin = user("2021003", "READER", "林可", "13800000013", "linke@stu.edu", hash, "NORMAL", null, now);
        long zhao = user("2021004", "READER", "赵一", "13800000014", "zhaoyi@stu.edu", hash, "NORMAL", null, now);
        long wu = user("2021005", "READER", "吴桐", "13800000015", "wutong@stu.edu", hash, "BLACKLISTED", "连续三次逾期未归还", now);

        profile(chen, "计算机学院", "软件工程", "2021");
        profile(zhou, "文学院", "汉语言文学", "2022");
        profile(lin, "外国语学院", "英语", "2021");
        profile(zhao, "数学学院", "统计学", "2023");
        profile(wu, "计算机学院", "人工智能", "2020");

        long book1 = book("9787111544937", "深入理解计算机系统", "Randal E. Bryant", "机械工业出版社", "128.00",
                "从程序员视角理解计算机系统。", "[\"计算机\",\"系统\"]");
        long book2 = book("9787020002207", "百年孤独", "加西亚·马尔克斯", "人民文学出版社", "55.00",
                "布恩迪亚家族七代人的孤独史。", "[\"文学\",\"小说\"]");
        long book3 = book("9787544253994", "解忧杂货店", "东野圭吾", "南海出版公司", "39.50",
                "一间能回答烦恼的杂货店。", "[\"文学\",\"小说\"]");
        long book4 = book("9787115428028", "算法导论", "Thomas H. Cormen", "人民邮电出版社", "128.00",
                "算法领域的经典教材。", "[\"计算机\",\"算法\"]");
        long book5 = book("9787506365437", "活着", "余华", "作家出版社", "28.00", "一个人一生的苦难与坚韧。", "[\"文学\"]");
        long book6 = book("9787121356392", "设计模式", "Erich Gamma", "电子工业出版社", "79.00",
                "面向对象设计的经典范式。", "[\"计算机\",\"设计\"]");

        long c1 = copy(book1, "BC0001", "A区-03-12", "ON_LOAN");
        copy(book1, "BC0002", "A区-03-13", "AVAILABLE");
        long c2 = copy(book2, "BC0003", "B区-01-08", "ON_LOAN");
        copy(book2, "BC0004", "B区-01-09", "AVAILABLE");
        long c3 = copy(book3, "BC0005", "B区-02-04", "ON_LOAN");
        long c4 = copy(book4, "BC0006", "A区-05-01", "AVAILABLE");
        long c5 = copy(book5, "BC0007", "B区-03-06", "ON_LOAN");
        long c6 = copy(book6, "BC0008", "A区-06-02", "ON_LOAN");

        long o1 = borrow("B20260101001", chen, book1, c1, now.minusDays(40), now.toLocalDate().minusDays(10),
                now.toLocalDate().minusDays(10), 0, 0, null, "OVERDUE");
        long o2 = borrow("B20260205002", zhou, book2, c2, now.minusDays(5), now.toLocalDate().plusDays(25),
                now.toLocalDate().plusDays(25), 0, 0, null, "BORROWED");
        long o3 = borrow("B20260208003", lin, book3, c3, now.minusDays(30), now.toLocalDate(),
                now.toLocalDate(), 0, 0, null, "BORROWED");
        long o4 = borrow("B20260120004", zhao, book4, c4, now.minusDays(30), now.toLocalDate().minusDays(2),
                now.toLocalDate().minusDays(2), 0, 0, now.minusHours(2), "RETURNED");
        borrow("B20260115005", chen, book5, c5, now.minusDays(33), now.toLocalDate().minusDays(3),
                now.toLocalDate().minusDays(3), 0, 0, null, "OVERDUE");
        borrow("B20261106006", lin, book6, c6, now.minusHours(1), now.toLocalDate().plusDays(30),
                now.toLocalDate().plusDays(30), 0, 0, null, "BORROWED");
        jdbc.update("UPDATE book_copy SET status = 'ON_LOAN' WHERE id IN (?,?,?,?,?)", c1, c2, c3, c5, c6);
        jdbc.update("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = ?", c4);

        reservation(book6, zhou, 1, "WAITING", now.minusDays(2));
        reservation(book1, lin, 2, "WAITING", now.minusDays(1));
        reservation(book4, zhao, 1, "READY", now.minusDays(1), now.minusHours(20), now.plusHours(28));

        grace("GA20260210001", o1, chen, "EXAM", "CALENDAR", 7, "0.85", "1.00", 5, "PENDING", null, now.minusHours(3));
        grace("GA20260209002", o4, zhao, "THESIS", "BOTH", 10, "1.00", "0.85", 9, "APPROVED", 9, now.minusDays(2));
        grace("GA20260208003", o2, zhou, "GRADUATION", "DECLARED", 14, "0.70", "0.70", 7, "REJECTED", null, now.minusDays(3));

        appeal("AP20260210001", chen, "OVERDUE_TIER", o1, "系统判定逾期不成立，考试周前后我在外地实习，已提前申请宽限。",
                "OPEN", now.plusHours(2), 0, null, now.minusHours(1));
        appeal("AP20260209002", zhou, "GRACE", 2L, "宽限天数与考试周不匹配，希望按校历重新核算。",
                "ACKED", now.minusHours(1), 1, librarianId, now.minusDays(1));

        task("RETURN_REMIND:" + o1 + ":L1", chen, o1, "L1", now.minusDays(13), "SENT");
        task("RETURN_REMIND:" + o1 + ":L2", chen, o1, "L2", now.minusDays(10), "SENT");
        task("OVERDUE:" + o1 + ":" + LocalDate.now(), chen, o1, "L3", now.minusHours(6), "SENT");
        task("OVERDUE:" + o2 + ":" + LocalDate.now(), zhou, o2, "L3", now.plusDays(1), "PLANNED");
        task("RETURN_REMIND:" + o3 + ":L1", lin, o3, "L1", now.minusDays(2), "SENT");
        task("RESERVATION_READY:3", zhao, null, "RESERVATION_READY", now.minusHours(20), "SENT");

        audit(adminId, "ADMIN", "RULE_UPDATE", "RULE", null, "调整宽限上限", "{\"GRACE.DMAX\":\"14\"}", "127.0.0.1",
                now.minusDays(1));
        audit(librarianId, "LIBRARIAN", "GRACE_APPROVE", "GRACE_APPLICATION", 2, "通过毕业季宽限申请",
                "{\"days\":9}", "127.0.0.1", now.minusDays(2));
        audit(librarianId, "LIBRARIAN", "APPEAL_ACK", "APPEAL_TICKET", 2, "受理申诉", "{}", "127.0.0.1",
                now.minusDays(1));
        audit(adminId, "ADMIN", "USER_BLACKLIST", "SYS_USER", wu, "拉黑违规读者",
                "{\"reason\":\"连续三次逾期未归还\"}", "127.0.0.1", now.minusDays(3));

        log.info("演示数据初始化完成：9 用户 / 6 书目 / 8 副本 / 5 借阅 / 3 预约 / 3 宽限 / 2 申诉");
    }

    /** 重建演示数据前清空业务表（仅 local profile 且 app.demo.reset=true 时调用）。 */
    private void clearDemoData() {
        for (String table : List.of(
                "event_outbox",
                "audit_log",
                "notify_delivery",
                "notify_task",
                "appeal_event",
                "appeal_ticket",
                "lost_book_report",
                "grace_application",
                "reservation",
                "renew_record",
                "borrow_order",
                "book_copy",
                "book",
                "user_status_log",
                "reader_credit",
                "reader_quiet_hours",
                "reader_channel_pref",
                "reader_profile",
                "sys_user")) {
            jdbc.update("DELETE FROM " + table);
        }
        log.info("已清空演示数据，准备重建");
    }

    private long user(
            String userNo,
            String role,
            String name,
            String phone,
            String email,
            String hash,
            String status,
            String blacklistReason,
            LocalDateTime now) {
        jdbc.update(
                """
                INSERT INTO sys_user (user_no, role, name, phone, email, password_hash, status, blacklist_reason,
                                      blacklisted_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                userNo,
                role,
                name,
                phone,
                email,
                hash,
                status,
                blacklistReason,
                blacklistReason == null ? null : now,
                now,
                now);
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE user_no = ?", Long.class, userNo);
    }

    private void profile(long userId, String department, String major, String grade) {
        jdbc.update(
                "INSERT INTO reader_profile (user_id, department, major, grade) VALUES (?, ?, ?, ?)",
                userId,
                department,
                major,
                grade);
    }

    private long book(
            String isbn, String title, String author, String publisher, String price, String summary, String tags) {
        jdbc.update(
                """
                INSERT INTO book (isbn13, title, author, publisher, publish_date, category_code, language, price,
                                  summary, tags, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, 'zh', ?, ?, ?, 1, NOW(), NOW())
                """,
                isbn,
                title,
                author,
                publisher,
                LocalDate.of(2016, 11, 1),
                "TP",
                new java.math.BigDecimal(price),
                summary,
                tags);
        return jdbc.queryForObject("SELECT id FROM book WHERE isbn13 = ?", Long.class, isbn);
    }

    private long copy(long bookId, String barcode, String location, String status) {
        jdbc.update(
                "INSERT INTO book_copy (book_id, barcode, location, status, acquired_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW())",
                bookId,
                barcode,
                location,
                status,
                LocalDate.now().minusYears(2));
        return jdbc.queryForObject("SELECT id FROM book_copy WHERE barcode = ?", Long.class, barcode);
    }

    private long borrow(
            String orderNo,
            long readerId,
            long bookId,
            long copyId,
            LocalDateTime borrowedAt,
            LocalDate dueAt,
            LocalDate effectiveDueAt,
            int graceDays,
            int renewCount,
            LocalDateTime returnedAt,
            String status) {
        jdbc.update(
                """
                INSERT INTO borrow_order (order_no, reader_id, book_id, copy_id, borrowed_at, due_at, effective_due_at,
                                          grace_days, renew_count, returned_at, final_overdue_days, status, return_condition,
                                          created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                orderNo,
                readerId,
                bookId,
                copyId,
                borrowedAt,
                dueAt,
                effectiveDueAt,
                graceDays,
                renewCount,
                returnedAt,
                returnedAt == null ? 0 : Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(effectiveDueAt,
                        returnedAt.toLocalDate())),
                status,
                returnedAt == null ? null : "GOOD",
                borrowedAt,
                borrowedAt);
        return jdbc.queryForObject("SELECT id FROM borrow_order WHERE order_no = ?", Long.class, orderNo);
    }

    private void reservation(long bookId, long readerId, int queueNo, String status, LocalDateTime createdAt) {
        reservation(bookId, readerId, queueNo, status, createdAt, null, null);
    }

    private void reservation(
            long bookId,
            long readerId,
            int queueNo,
            String status,
            LocalDateTime createdAt,
            LocalDateTime readyAt,
            LocalDateTime expiresAt) {
        jdbc.update(
                """
                INSERT INTO reservation (book_id, reader_id, queue_no, status, ready_at, expires_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                bookId,
                readerId,
                queueNo,
                status,
                readyAt,
                expiresAt,
                createdAt,
                createdAt);
    }

    private void grace(
            String applyNo,
            long orderId,
            long readerId,
            String scene,
            String source,
            int baseDays,
            String kt,
            String kq,
            int computedDays,
            String status,
            Integer grantedDays,
            LocalDateTime createdAt) {
        jdbc.update(
                """
                INSERT INTO grace_application (apply_no, borrow_order_id, reader_id, scene_code, scene_source, calendar_hit,
                                               base_days, kt, kq, dmax, computed_days, granted_days, evidence_text,
                                               formula_version, queue_len, snapshot_expires_at, status, decided_at,
                                               created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 14, ?, ?, ?, 'v1', 2, ?, ?, ?, ?, ?)
                """,
                applyNo,
                orderId,
                readerId,
                scene,
                source,
                "CALENDAR".equals(source) ? 1 : 0,
                new java.math.BigDecimal(baseDays),
                new java.math.BigDecimal(kt),
                new java.math.BigDecimal(kq),
                new java.math.BigDecimal(computedDays),
                grantedDays,
                "考试周与实习安排重叠，附实习证明与校历截图。",
                createdAt.plusHours(24),
                status,
                "PENDING".equals(status) ? null : createdAt.plusHours(1),
                createdAt,
                createdAt);
    }

    private void appeal(
            String ticketNo,
            long readerId,
            String targetType,
            long targetId,
            String reason,
            String status,
            LocalDateTime slaDueAt,
            int slaBreached,
            Long assigneeId,
            LocalDateTime createdAt) {
        jdbc.update(
                """
                INSERT INTO appeal_ticket (ticket_no, reader_id, target_type, target_id, reason, status, sla_due_at,
                                           first_reply_at, sla_breached, assignee_id, assignee_name, compensation_days,
                                           resolution, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
                """,
                ticketNo,
                readerId,
                targetType,
                targetId,
                reason,
                status,
                slaDueAt,
                "ACKED".equals(status) ? createdAt.plusMinutes(30) : null,
                slaBreached,
                assigneeId,
                assigneeId == null ? null : "李倩",
                "ACKED".equals(status) ? null : null,
                createdAt,
                createdAt);
    }

    private void task(
            String dedupKey, long readerId, Long orderId, String level, LocalDateTime planAt, String status) {
        jdbc.update(
                """
                INSERT INTO notify_task (dedup_key, reader_id, borrow_order_id, level, plan_at, quiet_shifted, status,
                                         payload, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?)
                """,
                dedupKey,
                readerId,
                orderId,
                level,
                planAt,
                status,
                "{\"orderId\":" + orderId + "}",
                planAt,
                planAt);
    }

    private void audit(
            Long actorId,
            String actorRole,
            String action,
            String objectType,
            Object objectId,
            String purpose,
            String detail,
            String ip,
            LocalDateTime at) {
        jdbc.update(
                """
                INSERT INTO audit_log (actor_id, actor_role, action, object_type, object_id, purpose, detail, ip, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                actorId,
                actorRole,
                action,
                objectType,
                objectId == null ? 0L : objectId,
                purpose,
                detail,
                ip,
                at);
    }

    @SuppressWarnings("unused")
    private List<String> demoNames() {
        return List.of("陈昊", "周雨", "林可", "赵一", "吴桐");
    }
}
