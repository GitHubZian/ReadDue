package com.zian.backend.rule.domain.repository;

import java.util.Map;
import java.util.Optional;

/**
 * 规则参数仓储（rule_param 键值表）。
 */
public interface RuleParamRepository {

    /**
     * 读取全部生效参数（键 → 值）。
     */

    Map<String, String> all();

    /**
     * 按键读取参数值。
     */

    Optional<String> find(String key);

    /**
     * 按旧版本做乐观锁更新，成功返回 true。
     */
    boolean updateValue(String key, String value, Long operatorId, String nextVersion);

    /**
     * 当前全局规则版本。
     */
    String currentVersion();

    /**
     * 下一个全局版本号。
     */
    String nextVersion();
}
