package com.zian.backend.bootstrap.demo;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 本地演示数据：仅在 local profile 且 app.demo.enabled=true 时执行，且库中没有任何用户时插入一次。
 *
 * <p>日期全部相对 now() 计算，保证看板里的「今日到期 / 逾期」永远有数据。
 */
@Component
@Profile("local")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = false)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_PASSWORD = "123456";

    private final DemoSeedMapper seed;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final boolean reset;

    public DemoDataInitializer(
            DemoSeedMapper seed,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate,
            @Value("${app.demo.reset:false}") boolean reset) {
        this.seed = seed;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
        this.reset = reset;
    }

    @Override
    public void run(String... args) {
        // 演示种子是本地开发夹具，用编程式事务保证「要么全写入、要么全回滚」；
        // 业务代码仍然只在 application 层用 @Transactional。
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    /** 在单个事务内写入演示数据；库中已有用户时跳过。 */
    private void seed() {
        if (reset) {
            clearDemoData();
        }
        if (seed.countUsers() > 0) {
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
                now.toLocalDate().minusDays(10), null, "OVERDUE");
        long o2 = borrow("B20260205002", zhou, book2, c2, now.minusDays(5), now.toLocalDate().plusDays(25),
                now.toLocalDate().plusDays(25), null, "BORROWED");
        long o3 = borrow("B20260208003", lin, book3, c3, now.minusDays(30), now.toLocalDate(),
                now.toLocalDate(), null, "BORROWED");
        long o4 = borrow("B20260120004", zhao, book4, c4, now.minusDays(30), now.toLocalDate().minusDays(2),
                now.toLocalDate().minusDays(2), now.minusHours(2), "RETURNED");
        borrow("B20260115005", chen, book5, c5, now.minusDays(33), now.toLocalDate().minusDays(3),
                now.toLocalDate().minusDays(3), null, "OVERDUE");
        borrow("B20261106006", lin, book6, c6, now.minusHours(1), now.toLocalDate().plusDays(30),
                now.toLocalDate().plusDays(30), null, "BORROWED");
        seed.updateCopyStatus(List.of(c1, c2, c3, c5, c6), "ON_LOAN");
        seed.updateCopyStatus(List.of(c4), "AVAILABLE");

        reservation(book6, zhou, 1, "WAITING", now.minusDays(2));
        reservation(book1, lin, 2, "WAITING", now.minusDays(1));
        reservation(book4, zhao, 1, "READY", now.minusDays(1), now.minusHours(20), now.plusHours(28));

        grace("GA20260210001", o1, chen, "EXAM", "CALENDAR", 7, "0.85", "1.00", 5, "PENDING", null,
                now.minusHours(3));
        long g2 = grace("GA20260209002", o4, zhao, "THESIS", "BOTH", 10, "1.00", "0.85", 9, "APPROVED", 9,
                now.minusDays(2));
        grace("GA20260208003", o2, zhou, "GRADUATION", "DECLARED", 14, "0.70", "0.70", 7, "REJECTED", null,
                now.minusDays(3));

        appeal("AP20260210001", chen, "OVERDUE_TIER", o1, "系统判定逾期不成立，考试周前后我在外地实习，已提前申请宽限。",
                "OPEN", now.plusHours(2), 0, null, now.minusHours(1));
        long a2 = appeal("AP20260209002", zhou, "GRACE", g2, "宽限天数与考试周不匹配，希望按校历重新核算。",
                "ACKED", now.minusHours(1), 1, librarianId, now.minusDays(1));

        task("RETURN_REMIND:" + o1 + ":L1", chen, o1, "L1", now.minusDays(13), "SENT");
        task("RETURN_REMIND:" + o1 + ":L2", chen, o1, "L2", now.minusDays(10), "SENT");
        task("OVERDUE:" + o1 + ":" + LocalDate.now(), chen, o1, "L3", now.minusHours(6), "SENT");
        task("OVERDUE:" + o2 + ":" + LocalDate.now(), zhou, o2, "L3", now.plusDays(1), "PLANNED");
        task("RETURN_REMIND:" + o3 + ":L1", lin, o3, "L1", now.minusDays(2), "SENT");
        task("RESERVATION_READY:3", zhao, null, "RESERVATION_READY", now.minusHours(20), "SENT");

        audit(adminId, "ADMIN", "RULE_UPDATE", "RULE", null, "调整宽限上限", "{\"GRACE.DMAX\":\"14\"}", "127.0.0.1",
                now.minusDays(1));
        audit(librarianId, "LIBRARIAN", "GRACE_APPROVE", "GRACE_APPLICATION", g2, "通过毕业季宽限申请",
                "{\"days\":9}", "127.0.0.1", now.minusDays(2));
        audit(librarianId, "LIBRARIAN", "APPEAL_ACK", "APPEAL_TICKET", a2, "受理申诉", "{}", "127.0.0.1",
                now.minusDays(1));
        audit(adminId, "ADMIN", "USER_BLACKLIST", "SYS_USER", wu, "拉黑违规读者",
                "{\"reason\":\"连续三次逾期未归还\"}", "127.0.0.1", now.minusDays(3));

        log.info("演示数据初始化完成：7 用户 / 6 书目 / 8 副本 / 5 借阅 / 3 预约 / 3 宽限 / 2 申诉");
    }

    /** 重建演示数据前清空业务表（仅 local profile 且 app.demo.reset=true 时调用）。 */
    private void clearDemoData() {
        seed.clearEventOutbox();
        seed.clearAuditLog();
        seed.clearNotifyDelivery();
        seed.clearNotifyTask();
        seed.clearAppealEvent();
        seed.clearAppealTicket();
        seed.clearLostBookReport();
        seed.clearGraceApplication();
        seed.clearReservation();
        seed.clearRenewRecord();
        seed.clearBorrowOrder();
        seed.clearBookCopy();
        seed.clearBook();
        seed.clearUserStatusLog();
        seed.clearReaderCredit();
        seed.clearReaderQuietHours();
        seed.clearReaderChannelPref();
        seed.clearReaderProfile();
        seed.clearSysUser();
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
        seed.insertUser(IdWorker.getId(), userNo, role, name, phone, email, hash, status, blacklistReason,
                blacklistReason == null ? null : now, now);
        return seed.findUserId(userNo);
    }

    private void profile(long userId, String department, String major, String grade) {
        seed.insertProfile(userId, department, major, grade);
    }

    private long book(
            String isbn, String title, String author, String publisher, String price, String summary, String tags) {
        seed.insertBook(IdWorker.getId(), isbn, title, author, publisher, LocalDate.of(2016, 11, 1), "TP",
                new BigDecimal(price), summary, tags);
        return seed.findBookId(isbn);
    }

    private long copy(long bookId, String barcode, String location, String status) {
        seed.insertCopy(IdWorker.getId(), bookId, barcode, location, status, LocalDate.now().minusYears(2));
        return seed.findCopyId(barcode);
    }

    private long borrow(
            String orderNo,
            long readerId,
            long bookId,
            long copyId,
            LocalDateTime borrowedAt,
            LocalDate dueAt,
            LocalDate effectiveDueAt,
            LocalDateTime returnedAt,
            String status) {
        int overdueDays = returnedAt == null
                ? 0
                : (int) Math.max(0, ChronoUnit.DAYS.between(effectiveDueAt, returnedAt.toLocalDate()));
        seed.insertBorrow(IdWorker.getId(), orderNo, readerId, bookId, copyId, borrowedAt, dueAt, effectiveDueAt,
                0, 0, returnedAt, overdueDays, status, returnedAt == null ? null : "GOOD");
        return seed.findBorrowId(orderNo);
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
        seed.insertReservation(IdWorker.getId(), bookId, readerId, queueNo, status, readyAt, expiresAt, createdAt);
    }

    private long grace(
            String applyNo,
            Long orderId,
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
        seed.insertGrace(IdWorker.getId(), applyNo, orderId, readerId, scene, source,
                "CALENDAR".equals(source) ? 1 : 0,
                new BigDecimal(baseDays), new BigDecimal(kt), new BigDecimal(kq),
                new BigDecimal(computedDays), grantedDays,
                "考试周与实习安排重叠，附实习证明与校历截图。",
                createdAt.plusHours(24), status,
                "PENDING".equals(status) ? null : createdAt.plusHours(1), createdAt);
        return Objects.requireNonNull(seed.findGraceId(applyNo), "宽限申请插入失败: " + applyNo);
    }

    private long appeal(
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
        seed.insertAppeal(IdWorker.getId(), ticketNo, readerId, targetType, targetId, reason, status, slaDueAt,
                "ACKED".equals(status) ? createdAt.plusMinutes(30) : null,
                slaBreached, assigneeId, assigneeId == null ? null : "李倩", createdAt);
        return Objects.requireNonNull(seed.findAppealId(ticketNo), "申诉工单插入失败: " + ticketNo);
    }

    private void task(
            String dedupKey, long readerId, Long orderId, String level, LocalDateTime planAt, String status) {
        seed.insertTask(IdWorker.getId(), dedupKey, readerId, orderId, level, planAt, status,
                "{\"orderId\":" + orderId + "}");
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
        seed.insertAudit(IdWorker.getId(), actorId, actorRole, action, objectType,
                objectId == null ? 0L : Long.valueOf(String.valueOf(objectId)),
                purpose, detail, ip, at);
    }
}
