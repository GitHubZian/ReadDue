package com.zian.backend.shared.file.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.shared.file.domain.model.StoredFile;
import com.zian.backend.shared.file.domain.repository.FileRepository;
import com.zian.backend.shared.file.infrastructure.persistence.mapper.FileObjectMapper;
import com.zian.backend.shared.file.infrastructure.persistence.po.FileObjectPO;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * {@link FileRepository} 的 MyBatis-Plus 实现，负责元数据 PO 与领域记录互转。
 */
@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileObjectMapper mapper;

    @Override
    public void insert(StoredFile file) {
        mapper.insert(toPo(file));
    }

    @Override
    public Optional<StoredFile> findById(long id) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<FileObjectPO>lambdaQuery()
                        .eq(FileObjectPO::getId, id)
                        .eq(FileObjectPO::getDeleted, 0)))
                .map(FileRepositoryImpl::toDomain);
    }

    @Override
    public List<StoredFile> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectList(Wrappers.<FileObjectPO>lambdaQuery()
                        .in(FileObjectPO::getId, ids)
                        .eq(FileObjectPO::getDeleted, 0))
                .stream()
                .map(FileRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public int markDeleted(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return mapper.update(null, Wrappers.<FileObjectPO>lambdaUpdate()
                .in(FileObjectPO::getId, ids)
                .set(FileObjectPO::getDeleted, 1));
    }

    private static FileObjectPO toPo(StoredFile file) {
        FileObjectPO po = new FileObjectPO();
        po.setId(file.id());
        po.setOwnerId(file.ownerId());
        po.setOriginalName(file.originalName());
        po.setContentType(file.contentType());
        po.setSizeBytes(file.sizeBytes());
        po.setSha256(file.sha256());
        po.setStoragePath(file.storagePath());
        po.setCreatedAt(file.createdAt());
        return po;
    }

    private static StoredFile toDomain(FileObjectPO po) {
        return new StoredFile(
                po.getId(),
                po.getOwnerId(),
                po.getOriginalName(),
                po.getContentType(),
                po.getSizeBytes() == null ? 0L : po.getSizeBytes(),
                po.getSha256(),
                po.getStoragePath(),
                po.getCreatedAt());
    }
}
