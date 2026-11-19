package com.zian.backend.catalog.infrastructure.persistence;

import com.zian.backend.catalog.domain.model.BookDetail;
import com.zian.backend.catalog.domain.model.BookSummary;
import com.zian.backend.catalog.domain.model.CopyInfo;
import com.zian.backend.catalog.domain.model.MyReservation;
import com.zian.backend.catalog.domain.model.StockSnapshot;
import com.zian.backend.catalog.domain.repository.BookQueryRepository;
import com.zian.backend.catalog.infrastructure.persistence.mapper.BookMapper;
import com.zian.backend.catalog.infrastructure.persistence.mapper.BookQueryMapper;
import com.zian.backend.catalog.infrastructure.persistence.po.BookPO;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 书目只读查询端口的 MyBatis 实现。
 *
 * <p>检索/库存/册直接由 SQL 映射为只读 record；详情需要标签 JSON 反序列化，交给 MyBatis-Plus
 * 的 {@link BookMapper} 读出 PO 后再转成领域 record，避免把 PO 暴露给 application。
 */
@Repository
@RequiredArgsConstructor
public class BookQueryRepositoryImpl implements BookQueryRepository {

    private final BookMapper bookMapper;
    private final BookQueryMapper bookQueryMapper;

    @Override
    public List<BookSummary> search(String keyword, long offset, int limit) {
        return bookQueryMapper.search(keyword, offset, limit);
    }

    @Override
    public long count(String keyword) {
        return bookQueryMapper.count(keyword);
    }

    @Override
    public Optional<BookDetail> findDetail(long bookId) {
        BookPO book = bookMapper.selectById(bookId);
        if (book == null) {
            return Optional.empty();
        }
        StockSnapshot stock = stock(bookId);
        return Optional.of(new BookDetail(
                book.getId(),
                book.getIsbn13(),
                book.getTitle(),
                book.getSubtitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublishDate(),
                book.getCategoryCode(),
                book.getLanguage(),
                book.getPrice(),
                book.getSummary(),
                book.getCoverUrl(),
                book.getTags() == null ? List.of() : List.copyOf(book.getTags()),
                book.getStatus() != null && book.getStatus() == 1,
                nz(stock.totalCopies()),
                nz(stock.availableCopies()),
                nz(stock.onLoanCopies()),
                nz(stock.queueLen())));
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    public List<CopyInfo> copies(long bookId) {
        return bookQueryMapper.copies(bookId);
    }

    @Override
    public StockSnapshot stock(long bookId) {
        StockSnapshot stock = bookQueryMapper.stock(bookId);
        return stock == null ? new StockSnapshot(0, 0, 0, 0) : stock;
    }

    @Override
    public Optional<MyReservation> myReservation(long bookId, long readerId) {
        return Optional.ofNullable(bookQueryMapper.myReservation(bookId, readerId));
    }
}
