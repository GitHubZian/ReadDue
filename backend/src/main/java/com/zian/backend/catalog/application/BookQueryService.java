package com.zian.backend.catalog.application;

import com.zian.backend.catalog.domain.model.BookDetail;
import com.zian.backend.catalog.domain.model.BookSummary;
import com.zian.backend.catalog.domain.repository.BookQueryRepository;
import com.zian.backend.catalog.interfaces.dto.BookDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 读者端书目检索与馆藏查询。
 *
 * <p>只读服务，不开事务：检索走视图 {@code v_book_stock}，库存与可借数以视图为唯一口径。
 */
@Service
@RequiredArgsConstructor
public class BookQueryService {

    private static final int MAX_KEYWORD_LENGTH = 100;

    private final BookQueryRepository bookQueryRepository;

    /**
     * 分页检索上架书目。
     *
     * <p>分页参数统一交给 {@link PageQuery} 校验：页码非法与 size 超过 200 都是 400，
     * 与全站列表保持一致，不做「按需放大页」的特例。
     *
     * @param q    关键词（书名/作者模糊、ISBN 精确），可空
     * @param page 页码，从 1 开始
     * @param size 每页条数，上限 200
     * @return 分页书目
     */
    public PageResult<BookDtos.BookItem> search(String q, Integer page, Integer size) {
        PageQuery query = PageQuery.of(page, size);
        String keyword = escapeLike(q);
        long total = bookQueryRepository.count(keyword);
        if (total == 0) {
            return PageResult.empty(query);
        }
        List<BookDtos.BookItem> items = bookQueryRepository
                .search(keyword, query.offset(), query.size())
                .stream()
                .map(BookQueryService::toItem)
                .toList();
        return PageResult.of(items, total, query);
    }

    /**
     * 查询书目详情。
     *
     * @param bookId 书目 ID
     * @return 详情（含库存与标签）
     * @throws BusinessException 书目不存在或已下架时抛 404
     */
    public BookDtos.BookDetail detail(long bookId) {
        BookDetail detail = bookQueryRepository.findDetail(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "书目不存在或已下架"));
        return new BookDtos.BookDetail(
                detail.id(),
                detail.isbn13(),
                detail.title(),
                detail.subtitle(),
                detail.author(),
                detail.publisher(),
                dateText(detail.publishDate()),
                detail.categoryCode(),
                detail.language(),
                detail.price() == null ? null : detail.price().toPlainString(),
                detail.summary(),
                detail.coverUrl(),
                detail.tags(),
                detail.onShelf(),
                detail.totalCopies(),
                detail.availableCopies(),
                detail.onLoanCopies(),
                detail.queueLen());
    }

    /**
     * 查询书目的全部馆藏册。
     *
     * @param bookId 书目 ID
     * @return 册列表（可借优先）
     * @throws BusinessException 书目不存在时抛 404
     */
    public List<BookDtos.CopyItem> copies(long bookId) {
        requireBook(bookId);
        return bookQueryRepository.copies(bookId).stream()
                .map(copy -> new BookDtos.CopyItem(
                        copy.id(),
                        copy.barcode(),
                        copy.location(),
                        copy.status(),
                        dateText(copy.acquiredAt())))
                .toList();
    }

    /**
     * 查询书目排队情况与本人的预约位置。
     *
     * @param bookId 书目 ID
     * @return 排队人数与本人位置（未预约时本人字段为 null）
     * @throws BusinessException 书目不存在时抛 404
     */
    public BookDtos.QueueView queue(long bookId) {
        requireBook(bookId);
        int waiting = nz(bookQueryRepository.stock(bookId).queueLen());
        return bookQueryRepository.myReservation(bookId, UserContext.requireId())
                .map(mine -> new BookDtos.QueueView(bookId, waiting, mine.queueNo(), mine.status()))
                .orElseGet(() -> new BookDtos.QueueView(bookId, waiting, null, null));
    }

    private void requireBook(long bookId) {
        if (bookQueryRepository.findDetail(bookId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "书目不存在或已下架");
        }
    }

    private static BookDtos.BookItem toItem(BookSummary book) {
        return new BookDtos.BookItem(
                book.id(),
                book.isbn13(),
                book.title(),
                book.subtitle(),
                book.author(),
                book.publisher(),
                book.categoryCode(),
                book.coverUrl(),
                book.total(),
                book.available(),
                book.waiting());
    }

    /**
     * 转义 LIKE 通配符并裁剪空白；空白关键词视为「不过滤」。
     *
     * <p>MySQL 默认以反斜杠为 LIKE 转义符，因此 {@code %} 与 {@code _} 逐一加反斜杠，
     * 避免读者输入 {@code %} 把整库扫出来。
     */
    private static String escapeLike(String q) {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            trimmed = trimmed.substring(0, MAX_KEYWORD_LENGTH);
        }
        return trimmed.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static String dateText(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
