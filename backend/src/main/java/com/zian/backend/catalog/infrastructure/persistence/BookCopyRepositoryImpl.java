package com.zian.backend.catalog.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.catalog.domain.model.BookCopy;
import com.zian.backend.catalog.domain.model.CopyStatus;
import com.zian.backend.catalog.domain.repository.BookCopyRepository;
import com.zian.backend.catalog.infrastructure.persistence.mapper.BookCopyMapper;
import com.zian.backend.catalog.infrastructure.persistence.po.BookCopyPO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 馆藏副本仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class BookCopyRepositoryImpl implements BookCopyRepository {

    private final BookCopyMapper mapper;

    @Override
    public Optional<BookCopy> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<BookCopy> findByBarcode(String barcode) {
        BookCopyPO po = mapper.selectOne(Wrappers.<BookCopyPO>lambdaQuery()
                .eq(BookCopyPO::getBarcode, barcode)
                .eq(BookCopyPO::getDeleted, 0L)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public int update(BookCopy copy) {
        BookCopyPO po = new BookCopyPO();
        po.setId(copy.getId());
        po.setStatus(copy.getStatus().name());
        po.setVersion(copy.getVersion());
        return mapper.updateById(po);
    }

    @Override
    public Optional<BookCopy> findFirstAvailable(Long bookId) {
        BookCopyPO po = mapper.selectOne(Wrappers.<BookCopyPO>lambdaQuery()
                .eq(BookCopyPO::getBookId, bookId)
                .eq(BookCopyPO::getStatus, CopyStatus.AVAILABLE.name())
                .eq(BookCopyPO::getDeleted, 0L)
                .orderByAsc(BookCopyPO::getId)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<BookCopy> findFirstHeld(Long bookId) {
        BookCopyPO po = mapper.selectOne(Wrappers.<BookCopyPO>lambdaQuery()
                .eq(BookCopyPO::getBookId, bookId)
                .eq(BookCopyPO::getStatus, CopyStatus.READY_SHELF.name())
                .eq(BookCopyPO::getDeleted, 0L)
                .orderByAsc(BookCopyPO::getId)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    private BookCopy toDomain(BookCopyPO po) {
        return new BookCopy(
                po.getId(),
                po.getBookId(),
                po.getBarcode(),
                po.getLocation(),
                CopyStatus.valueOf(po.getStatus()),
                po.getVersion());
    }
}
