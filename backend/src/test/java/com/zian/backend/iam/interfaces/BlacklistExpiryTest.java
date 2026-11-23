package com.zian.backend.iam.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.iam.application.BlacklistExpiryService;
import com.zian.backend.security.SecurityTestSupport;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * B-18 拉黑到期：到期视为 NORMAL、扫描恢复并留痕；未到期仍拦截借阅。
 */
class BlacklistExpiryTest extends SecurityTestSupport {

    @Autowired
    private BlacklistExpiryService expiryService;

    @Test
    void scanRestoresExpiredBlacklistWithLogAndAudit() {
        String readerNo = newAccount("bl");
        long readerId = createUser(readerNo, "READER", "NORMAL", null);
        jdbc.update(
                "UPDATE sys_user SET status='BLACKLISTED', blacklist_reason='到期测试', "
                        + "blacklist_expires_at=DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE id=?",
                readerId);

        expiryService.restoreExpired();

        assertThat(statusOf(readerId)).isEqualTo("NORMAL");
        assertThat(count(
                        "SELECT COUNT(*) FROM user_status_log WHERE user_id=? "
                                + "AND from_status='BLACKLISTED' AND to_status='NORMAL' "
                                + "AND reason='到期自动解除' AND operator_name='系统'",
                        readerId))
                .isEqualTo(1);
        assertThat(count(
                        "SELECT COUNT(*) FROM audit_log WHERE object_id=? AND action='USER_UNBLOCK_EXPIRE'",
                        readerId))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT blacklist_expires_at FROM sys_user WHERE id=?", Object.class, readerId))
                .isNull();
    }

    @Test
    void scanKeepsNotYetExpiredBlacklist() {
        String readerNo = newAccount("bl");
        long readerId = createUser(readerNo, "READER", "NORMAL", null);
        jdbc.update(
                "UPDATE sys_user SET status='BLACKLISTED', blacklist_reason='未到期', "
                        + "blacklist_expires_at=DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE id=?",
                readerId);

        expiryService.restoreExpired();

        assertThat(statusOf(readerId)).isEqualTo("BLACKLISTED");
        assertThat(count("SELECT COUNT(*) FROM user_status_log WHERE user_id=?", readerId))
                .isZero();
    }

    @Test
    void expiredBlacklistDoesNotBlockBorrowBeforeScan() throws Exception {
        String readerNo = newAccount("bl");
        long readerId = createUser(readerNo, "READER", "NORMAL", null);
        jdbc.update(
                "UPDATE sys_user SET status='BLACKLISTED', blacklist_reason='已到期', "
                        + "blacklist_expires_at=DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE id=?",
                readerId);
        Fixture fixture = createBookWithCopy("AVAILABLE");

        mockMvc.perform(post("/v1/borrows")
                        .header("Authorization", bearer(login(readerNo)))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("readerNo", readerNo, "barcode", fixture.barcode()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void adminBlacklistWritesStatusLog() throws Exception {
        String adminNo = newAccount("adm");
        createUser(adminNo, "ADMIN", "NORMAL", null);
        String readerNo = newAccount("bl");
        long readerId = createUser(readerNo, "READER", "NORMAL", null);

        String expiresAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS).toString();
        mockMvc.perform(post("/v1/admin/users/{id}/blacklist", readerId)
                        .header("Authorization", bearer(login(adminNo)))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("reason", "测试拉黑", "expiresAt", expiresAt))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("BLACKLISTED"));

        assertThat(count(
                        "SELECT COUNT(*) FROM user_status_log WHERE user_id=? "
                                + "AND from_status='NORMAL' AND to_status='BLACKLISTED' "
                                + "AND reason='测试拉黑' AND expires_at IS NOT NULL",
                        readerId))
                .isEqualTo(1);
    }

    private String statusOf(long userId) {
        return jdbc.queryForObject("SELECT status FROM sys_user WHERE id=?", String.class, userId);
    }

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }
}
