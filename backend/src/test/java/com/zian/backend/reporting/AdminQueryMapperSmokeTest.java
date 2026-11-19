package com.zian.backend.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import com.zian.backend.reporting.domain.model.GraceRow;
import com.zian.backend.reporting.domain.model.OverdueRow;
import com.zian.backend.reporting.domain.model.UserRow;
import com.zian.backend.reporting.domain.repository.AdminReadRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 读模型 record 映射冒烟：record 组件名与 SQL 别名不一致时 MyBatis 会静默映射成 {@code null}，
 * 这里用真实库数据断言关键字段非空，防止重命名后悄悄退化。
 */
@SpringBootTest
class AdminQueryMapperSmokeTest {

    @Autowired
    private AdminReadRepository readRepository;

    /**
     * 宽限列表的首行应带出申请单号、读者姓名与书目信息。
     */
    @Test
    void gracesShouldMapReaderAndBook() {
        List<GraceRow> rows = readRepository.graces(null, null, 5, 0);
        assertThat(rows).as("本地演示库应含宽限数据").isNotEmpty();
        GraceRow row = rows.getFirst();
        assertThat(row.id()).isNotNull();
        assertThat(row.applyNo()).isNotBlank();
        assertThat(row.readerName()).isNotBlank();
        assertThat(row.title()).isNotBlank();
        assertThat(row.computedDays()).isNotNull();
    }

    /**
     * 逾期看板首行应带出订单、读者、书目与逾期天数（视图列全为可空，靠 SQL 过滤保证非空）。
     */
    @Test
    void overdueShouldMapViewColumns() {
        List<OverdueRow> rows = readRepository.overdue(null, null, 5, 0);
        assertThat(rows).as("本地演示库应含逾期数据").isNotEmpty();
        OverdueRow row = rows.getFirst();
        assertThat(row.orderId()).isNotNull();
        assertThat(row.readerName()).isNotBlank();
        assertThat(row.bookTitle()).isNotBlank();
        assertThat(row.overdueDays()).isNotNull();
        assertThat(row.l3Sent()).isNotNull();
    }

    /**
     * 用户列表首行应带出姓名、角色与借阅统计。
     */
    @Test
    void usersShouldMapCounters() {
        List<UserRow> rows = readRepository.users(null, null, null, 5, 0);
        assertThat(rows).as("本地演示库应含用户数据").isNotEmpty();
        UserRow row = rows.getFirst();
        assertThat(row.id()).isNotNull();
        assertThat(row.name()).isNotBlank();
        assertThat(row.role()).isNotBlank();
        assertThat(row.borrowCount()).isNotNull();
    }
}
