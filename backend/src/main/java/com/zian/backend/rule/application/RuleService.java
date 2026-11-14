package com.zian.backend.rule.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.rule.domain.repository.RuleParamRepository;
import com.zian.backend.rule.interfaces.dto.RuleDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 规则参数应用服务：读取（带类型转换）与批量更新。
 */
@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleParamRepository repository;
    private final AuditRecorder auditRecorder;

    /**
     * 读取全部生效规则参数。
     *
     * @return 参数键到值的映射
     */
    @Transactional(readOnly = true)
    public Map<String, String> all() {
        return repository.all();
    }

    /**
     * 当前全局规则版本。
     *
     * @return 版本号，如 v12
     */
    @Transactional(readOnly = true)
    public String version() {
        return repository.currentVersion();
    }

    /**
     * 读取字符串参数，缺失时返回默认值。
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    @Transactional(readOnly = true)
    public String text(String key, String defaultValue) {
        return repository.find(key).orElse(defaultValue);
    }

    /**
     * 读取整型参数，缺失或非数字时返回默认值。
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    @Transactional(readOnly = true)
    public int intValue(String key, int defaultValue) {
        return repository.find(key).map(raw -> parseInt(raw, key)).orElse(defaultValue);
    }

    /**
     * 读取小数参数，缺失或非法时返回默认值。
     *
     * @param key          参数键
     * @param defaultValue 默认值（字符串形式）
     * @return 参数值
     */
    @Transactional(readOnly = true)
    public BigDecimal decimal(String key, String defaultValue) {
        return repository.find(key).map(raw -> parseDecimal(raw, key)).orElse(new BigDecimal(defaultValue));
    }

    /**
     * 保存规则参数：校验值域，按 expectedVersion 做乐观锁并整体推进版本。
     *
     * @param request 规则分组与期望版本
     * @return 新版本号
     */
    @Transactional
    public String update(RuleDtos.RuleUpdateRequest request) {
        String current = repository.currentVersion();
        if (request.expectedVersion() != null
                && !request.expectedVersion().isBlank()
                && !request.expectedVersion().equals(current)) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "规则已被其他人修改，请刷新后重试");
        }
        Long operatorId = UserContext.requireId();
        Map<String, String> existing = repository.all();
        String nextVersion = repository.nextVersion();
        Map<String, Object> changed = new LinkedHashMap<>();
        int updated = 0;
        if (request.groups() != null) {
            for (RuleDtos.RuleGroup group : request.groups()) {
                if (group == null || group.items() == null) {
                    continue;
                }
                for (RuleDtos.RuleItem item : group.items()) {
                    if (item == null || item.key() == null || item.value() == null) {
                        continue;
                    }
                    if (!existing.containsKey(item.key())) {
                        continue;
                    }
                    validate(item);
                    if (!repository.updateValue(item.key(), item.value(), operatorId, nextVersion)) {
                        throw new BusinessException(ErrorCode.CONFLICT_ERROR, "规则已被其他人修改，请刷新后重试");
                    }
                    updated++;
                    changed.put(item.key(), item.value());
                }
            }
        }
        auditRecorder.record("RULE_UPDATE", "RULE", null, "规则调整", Map.of("updated", updated, "changes", changed));
        return repository.currentVersion();
    }

    /** 值域校验：数值类必须为 >= 0 的数字，布尔类只接受 true/false */
    private void validate(RuleDtos.RuleItem item) {
        String type = item.type() == null ? "" : item.type();
        String raw = item.value().trim();
        switch (type) {
            case "number", "money" -> {
                try {
                    if (new BigDecimal(raw).signum() < 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, item.label() + " 不能为负数");
                    }
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, item.label() + " 必须是数字：" + raw);
                }
            }
            case "boolean" -> {
                if (!"true".equalsIgnoreCase(raw) && !"false".equalsIgnoreCase(raw)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, item.label() + " 必须是 true 或 false");
                }
            }
            default -> {
                // select / 文本参数不做值域限制，由前端选项约束
            }
        }
    }

    private int parseInt(String raw, String key) {
        try {
            return new BigDecimal(raw.trim()).intValue();
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "规则参数 " + key + " 配置不是合法数字：" + raw);
        }
    }

    private BigDecimal parseDecimal(String raw, String key) {
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "规则参数 " + key + " 配置不是合法数字：" + raw);
        }
    }
}
