package com.zian.backend.rule.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.rule.domain.repository.RuleParamRepository;
import com.zian.backend.rule.infrastructure.persistence.mapper.RuleParamMapper;
import com.zian.backend.rule.infrastructure.persistence.po.RuleParamPO;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 规则参数仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RuleParamRepositoryImpl implements RuleParamRepository {

    private final RuleParamMapper mapper;

    @Override
    public Map<String, String> all() {
        List<RuleParamPO> rows = mapper.selectList(Wrappers.<RuleParamPO>lambdaQuery()
                .and(w -> w.isNull(RuleParamPO::getEffectiveTo).or().gt(RuleParamPO::getEffectiveTo, LocalDateTime.now()))
                .orderByAsc(RuleParamPO::getParamKey));
        Map<String, String> result = new LinkedHashMap<>();
        rows.forEach(row -> result.put(row.getParamKey(), row.getParamValue()));
        return result;
    }

    @Override
    public Optional<String> find(String key) {
        RuleParamPO po = mapper.selectOne(Wrappers.<RuleParamPO>lambdaQuery()
                .eq(RuleParamPO::getParamKey, key)
                .orderByDesc(RuleParamPO::getEffectiveFrom)
                .last("limit 1"));
        return Optional.ofNullable(po).map(RuleParamPO::getParamValue);
    }

    @Override
    public boolean updateValue(String key, String value, Long operatorId, String nextVersion) {
        RuleParamPO po = mapper.selectOne(Wrappers.<RuleParamPO>lambdaQuery()
                .eq(RuleParamPO::getParamKey, key)
                .orderByDesc(RuleParamPO::getEffectiveFrom)
                .last("limit 1"));
        if (po == null) {
            return false;
        }
        int rows = mapper.update(null, Wrappers.<RuleParamPO>lambdaUpdate()
                .eq(RuleParamPO::getId, po.getId())
                .eq(RuleParamPO::getVersion, po.getVersion())
                .set(RuleParamPO::getParamValue, value)
                .set(RuleParamPO::getVersion, nextVersion)
                .set(RuleParamPO::getUpdatedBy, operatorId)
                .set(RuleParamPO::getUpdatedAt, LocalDateTime.now()));
        return rows > 0;
    }

    @Override
    public String nextVersion() {
        return "v" + (maxVersion() + 1);
    }

    @Override
    public String currentVersion() {
        return "v" + maxVersion();
    }

    private int maxVersion() {
        List<RuleParamPO> rows = mapper.selectList(Wrappers.<RuleParamPO>lambdaQuery()
                .select(RuleParamPO::getVersion));
        int max = 1;
        for (RuleParamPO row : rows) {
            int value = parseVersion(row.getVersion());
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private int parseVersion(String version) {
        if (version == null || !version.matches("v\\d+")) {
            return 1;
        }
        return Integer.parseInt(version.substring(1));
    }
}
