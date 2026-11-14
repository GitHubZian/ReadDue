package com.zian.backend.catalog.domain.repository;

import com.zian.backend.catalog.domain.model.BookCopy;
import java.util.Optional;

/**
 * 馆藏副本仓储。
 */
public interface BookCopyRepository {

    /**
     * 按 ID 查副本。
     */

    Optional<BookCopy> findById(Long id);

    /**
     * 按条码查副本。
     */

    Optional<BookCopy> findByBarcode(String barcode);

    /**
     * 回写副本状态与版本，返回影响行数。
     */
    int update(BookCopy copy);

    /**
     * 查该书目第一本可借副本。
     */
    Optional<BookCopy> findFirstAvailable(Long bookId);
}
