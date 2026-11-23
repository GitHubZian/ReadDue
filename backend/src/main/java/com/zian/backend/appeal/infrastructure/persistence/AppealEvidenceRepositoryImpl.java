package com.zian.backend.appeal.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.appeal.domain.repository.AppealEvidenceRepository;
import com.zian.backend.appeal.infrastructure.persistence.mapper.AppealEvidenceMapper;
import com.zian.backend.appeal.infrastructure.persistence.po.AppealEvidencePO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * {@link AppealEvidenceRepository} 的 MyBatis-Plus 实现。
 */
@Repository
@RequiredArgsConstructor
public class AppealEvidenceRepositoryImpl implements AppealEvidenceRepository {

    private final AppealEvidenceMapper mapper;

    @Override
    public void attach(long appealId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long fileId : fileIds) {
            AppealEvidencePO po = new AppealEvidencePO();
            po.setId(IdWorker.getId());
            po.setAppealId(appealId);
            po.setFileId(fileId);
            po.setCreatedAt(now);
            mapper.insert(po);
        }
    }

    @Override
    public List<Long> fileIdsOf(long appealId) {
        return fileIdsOf(List.of(appealId)).getOrDefault(appealId, List.of());
    }

    @Override
    public Map<Long, List<Long>> fileIdsOf(List<Long> appealIds) {
        if (appealIds == null || appealIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> grouped = new LinkedHashMap<>();
        mapper.selectList(Wrappers.<AppealEvidencePO>lambdaQuery()
                        .in(AppealEvidencePO::getAppealId, appealIds)
                        .orderByAsc(AppealEvidencePO::getCreatedAt))
                .forEach(po -> grouped
                        .computeIfAbsent(po.getAppealId(), key -> new ArrayList<>())
                        .add(po.getFileId()));
        return grouped;
    }
}
