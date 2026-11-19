package com.zian.backend.grace.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.grace.domain.repository.GraceEvidenceRepository;
import com.zian.backend.grace.infrastructure.persistence.mapper.GraceEvidenceMapper;
import com.zian.backend.grace.infrastructure.persistence.po.GraceEvidencePO;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * {@link GraceEvidenceRepository} 的 MyBatis-Plus 实现。
 */
@Repository
@RequiredArgsConstructor
public class GraceEvidenceRepositoryImpl implements GraceEvidenceRepository {

    private final GraceEvidenceMapper mapper;

    @Override
    public void attach(long graceId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long fileId : fileIds) {
            GraceEvidencePO po = new GraceEvidencePO();
            po.setId(IdWorker.getId());
            po.setGraceId(graceId);
            po.setFileId(fileId);
            po.setCreatedAt(now);
            mapper.insert(po);
        }
    }

    @Override
    public List<Long> fileIdsOf(long graceId) {
        return mapper
                .selectList(com.baomidou.mybatisplus.core.toolkit.Wrappers.<GraceEvidencePO>lambdaQuery()
                        .eq(GraceEvidencePO::getGraceId, graceId)
                        .orderByAsc(GraceEvidencePO::getCreatedAt))
                .stream()
                .map(GraceEvidencePO::getFileId)
                .toList();
    }

    @Override
    public Map<Long, List<Long>> fileIdsOf(List<Long> graceIds) {
        if (graceIds == null || graceIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> grouped = new LinkedHashMap<>();
        mapper.selectList(com.baomidou.mybatisplus.core.toolkit.Wrappers.<GraceEvidencePO>lambdaQuery()
                        .in(GraceEvidencePO::getGraceId, graceIds)
                        .orderByAsc(GraceEvidencePO::getCreatedAt))
                .forEach(po -> grouped.computeIfAbsent(po.getGraceId(), key -> new java.util.ArrayList<>())
                        .add(po.getFileId()));
        return grouped;
    }
}
