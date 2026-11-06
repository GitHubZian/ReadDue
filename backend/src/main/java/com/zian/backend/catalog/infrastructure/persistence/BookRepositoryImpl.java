package com.zian.backend.catalog.infrastructure.persistence;

import com.zian.backend.catalog.domain.model.Book;
import com.zian.backend.catalog.domain.repository.BookRepository;
import com.zian.backend.catalog.infrastructure.persistence.mapper.BookMapper;
import com.zian.backend.catalog.infrastructure.persistence.po.BookPO;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 书目仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    private final BookMapper mapper;

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<Book> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectBatchIds(ids).stream().map(this::toDomain).toList();
    }

    private Book toDomain(BookPO po) {
        return new Book(po.getId(), po.getTitle(), po.getAuthor(), po.getCoverUrl());
    }
}
