package com.zian.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.zian.backend.iam.infrastructure.persistence.mapper.SysRoleMapper;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyTemplateMapper;
import com.zian.backend.rule.infrastructure.persistence.mapper.RuleParamMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 启动冒烟：确认 MapperScan、PO 表映射与 MyBatis-Plus 会话工厂均可用。
 */
@SpringBootTest
class MapperSmokeTest {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private RuleParamMapper ruleParamMapper;

    @Autowired
    private NotifyTemplateMapper notifyTemplateMapper;

    @Test
    void mybatisPlusMappersAreWired() {
        assertThat(sysRoleMapper.selectCount(null)).isEqualTo(3L);
        assertThat(ruleParamMapper.selectCount(null)).isGreaterThanOrEqualTo(26L);
        assertThat(notifyTemplateMapper.selectCount(null)).isGreaterThanOrEqualTo(8L);
    }
}
