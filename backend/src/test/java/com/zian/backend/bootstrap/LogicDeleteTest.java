package com.zian.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.zian.backend.iam.infrastructure.persistence.mapper.SysRoleMapper;
import com.zian.backend.iam.infrastructure.persistence.po.SysRolePO;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 校验两件基础契约：雪花主键 ASSIGN_ID，以及逻辑删除把本行 id 写入 deleted。
 */
@SpringBootTest
@Transactional
class LogicDeleteTest {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void assignIdAndLogicDeleteWritePrimaryKey() {
        SysRolePO role = new SysRolePO();
        role.setRoleCode("TMP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        role.setRoleName("临时角色");
        role.setDataScope("SELF");
        role.setEnabled(1);

        assertThat(sysRoleMapper.insert(role)).isEqualTo(1);
        assertThat(role.getId()).isNotNull().isPositive();

        assertThat(sysRoleMapper.deleteById(role.getId())).isEqualTo(1);
        assertThat(sysRoleMapper.selectById(role.getId())).isNull();

        Long deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM sys_role WHERE id = ?", Long.class, role.getId());
        assertThat(deleted).isEqualTo(role.getId());
    }
}
