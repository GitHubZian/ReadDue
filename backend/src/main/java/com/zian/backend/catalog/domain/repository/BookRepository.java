package com.zian.backend.catalog.domain.repository;

import com.zian.backend.catalog.domain.model.Book;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 书目仓储（只读为主，展示用）。
 */
public interface BookRepository {

    /**
     * 按 ID 查书目。
     */

    Optional<Book> findById(Long id);

    /**
     * 批量查书目。
     */

    List<Book> findByIds(Collection<Long> ids);
}
