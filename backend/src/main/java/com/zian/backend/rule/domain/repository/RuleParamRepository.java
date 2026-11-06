package com.zian.backend.rule.domain.repository;

import java.util.Map;
import java.util.Optional;

/**
 * 规则参数仓储（rule_param 键值表）。
 */
public interface RuleParamRepository {

    Map<String, String> all();

    Optional<String> find(String key);

    /** 更新参数值并写版本号；返回是否命中该参数 */
    boolean updateValue(String key, String value, Long operatorId, String nextVersion);

    /** 当前参数版本（取最大 version，用于前端乐观并发） */
    String currentVersion();

    /** 下一次批量更新使用的版本号，使同一次请求内所有参数共享一个版本 */
    String nextVersion();
}
