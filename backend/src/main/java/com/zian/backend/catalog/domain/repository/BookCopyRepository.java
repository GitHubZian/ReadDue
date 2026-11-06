package com.zian.backend.catalog.domain.repository;

import com.zian.backend.catalog.domain.model.BookCopy;
import java.util.Optional;

/**
 * 馆藏副本仓储。
 */
public interface BookCopyRepository {

    Optional<BookCopy> findById(Long id);

    Optional<BookCopy> findByBarcode(String barcode);

    /** 乐观锁更新；返回 0 表示并发冲突 */
    int update(BookCopy copy);

    /** 该书目下第一本可借副本（借出时自动分配） */
    Optional<BookCopy> findFirstAvailable(Long bookId);
}
