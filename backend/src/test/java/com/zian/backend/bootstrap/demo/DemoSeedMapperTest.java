package com.zian.backend.bootstrap.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 校验演示种子 Mapper 的 SQL 绑定（语句只在重建演示数据时执行，普通启动覆盖不到）。
 *
 * <p>V4 迁移去掉了自增，所有种子插入都必须显式带雪花 id，这里顺带断言 id 被原样写入。
 */
@SpringBootTest
@Transactional
class DemoSeedMapperTest {

    @Autowired
    private DemoSeedMapper seed;

    @Test
    void insertsUserAndAuditWithBoundParameters() {
        LocalDateTime now = LocalDateTime.now();
        long userId = IdWorker.getId();
        seed.insertUser(
                userId, "TEST_SEED_001", "READER", "测试读者", "13900000000", "seed@test.local",
                "$2a$10$abcdefghijklmnopqrstuv", "NORMAL", null, null, now);

        Long id = seed.findUserId("TEST_SEED_001");
        assertThat(id).isEqualTo(userId);

        int rows = seed.insertAudit(
                IdWorker.getId(), id, "READER", "SEED_TEST", "SYS_USER", 0L, "验证种子 SQL", "{}", "127.0.0.1", now);
        assertThat(rows).isEqualTo(1);
    }
}
