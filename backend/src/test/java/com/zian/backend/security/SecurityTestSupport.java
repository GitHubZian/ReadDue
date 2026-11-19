package com.zian.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 安全回归测试基类。
 *
 * <p>所有测试运行在 {@code @Transactional} 事务内并回滚，不污染本地演示数据；需要的账号 / 书目 / 副本
 * 由用例自行插入，不依赖 {@code DemoDataInitializer} 是否已执行。
 *
 * <p>本类是测试脚手架而非测试用例，名称不以 {@code Test} 结尾，不会被 Surefire 收集。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class SecurityTestSupport {

    protected static final String JSON = "application/json";
    protected static final String DEMO_PASSWORD = "123456";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /** 生成不会与演示数据冲突的账号。 */
    protected String newAccount(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    protected long createUser(String userNo, String role, String status, String blacklistReason) {
        long id = IdWorker.getId();
        jdbc.update(
                """
                INSERT INTO sys_user (id, user_no, role, name, password_hash, status, blacklist_reason, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """,
                id,
                userNo,
                role,
                "安全审计-" + role,
                passwordEncoder.encode(DEMO_PASSWORD),
                status,
                blacklistReason);
        return id;
    }

    /** 插入一本书与一册馆藏，返回可用于借还的副本。 */
    protected Fixture createBookWithCopy(String copyStatus) {
        String isbn = "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String barcode = "TBC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        long bookId = IdWorker.getId();
        long copyId = IdWorker.getId();
        jdbc.update("INSERT INTO book (id, isbn13, title, status) VALUES (?, ?, ?, 1)", bookId, isbn, "安全审计测试书目");
        jdbc.update(
                "INSERT INTO book_copy (id, book_id, barcode, status, acquired_at) VALUES (?, ?, ?, ?, CURDATE())",
                copyId,
                bookId,
                barcode,
                copyStatus);
        return new Fixture(bookId, copyId, barcode);
    }

    /** 直接建一张在借单，用于验证「黑名单拦截借阅、但不拦截归还」。 */
    protected long createActiveLoan(long readerId, Fixture fixture) {
        String orderNo = "TO" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        LocalDate due = LocalDate.now().plusDays(20);
        long id = IdWorker.getId();
        jdbc.update(
                """
                INSERT INTO borrow_order (id, order_no, reader_id, book_id, copy_id, borrowed_at, due_at,
                                          effective_due_at, status)
                VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, 'BORROWED')
                """,
                id,
                orderNo,
                readerId,
                fixture.bookId(),
                fixture.copyId(),
                due,
                due);
        return id;
    }

    /** 走真实登录接口换取 JWT，顺带覆盖「登录 → 授权头」这一段链路。 */
    protected String login(String userNo) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("userNo", userNo, "password", DEMO_PASSWORD));
        String body = mockMvc.perform(post("/v1/auth/login").contentType(JSON).content(payload))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(body);
        if (node.path("code").asInt() != 0) {
            throw new IllegalStateException("登录失败：" + body);
        }
        return node.path("data").path("token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected record Fixture(long bookId, long copyId, String barcode) {}
}
