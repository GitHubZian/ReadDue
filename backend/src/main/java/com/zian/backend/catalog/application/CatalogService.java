package com.zian.backend.catalog.application;

import com.zian.backend.catalog.domain.model.Book;
import com.zian.backend.catalog.domain.model.BookCopy;
import com.zian.backend.catalog.domain.repository.BookCopyRepository;
import com.zian.backend.catalog.domain.repository.BookRepository;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.util.Collection;
import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 馆藏应用服务：借还台与其它上下文唯一的副本/书目访问入口。
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public BookCopy requireCopyByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "条码不能为空");
        }
        return copyRepository
                .findByBarcode(barcode.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到条码为 " + barcode + " 的馆藏"));
    }

    @Transactional(readOnly = true)
    public Book requireBook(Long bookId) {
        return bookRepository
                .findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "书目不存在"));
    }

    @Transactional(readOnly = true)
    public Map<Long, Book> booksByIds(Collection<Long> ids) {
        return bookRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(Book::id, Function.identity(), (a, b) -> a));
    }

    @Transactional(readOnly = true)
    public List<Book> books(Collection<Long> ids) {
        return bookRepository.findByIds(ids);
    }

    /** 借出：副本置为在借。 */
    @Transactional
    public BookCopy lend(Long copyId) {
        BookCopy copy = copyRepository
                .findById(copyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "馆藏副本不存在"));
        copy.lend();
        if (copyRepository.update(copy) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "馆藏副本状态已被修改，请重试");
        }
        return copy;
    }

    /** 归还：正常回架 / 损坏转维修。 */
    @Transactional
    public BookCopy receive(Long copyId, boolean damaged) {
        BookCopy copy = copyRepository
                .findById(copyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "馆藏副本不存在"));
        copy.receive(damaged);
        if (copyRepository.update(copy) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "馆藏副本状态已被修改，请重试");
        }
        return copy;
    }

    /** 预约到书：把某本可借副本预留在架。 */
    @Transactional
    public BookCopy holdForReservation(Long bookId) {
        BookCopy copy = copyRepository
                .findFirstAvailable(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RULE_VIOLATION, "该书目暂无可预留副本"));
        copy.holdForReservation();
        if (copyRepository.update(copy) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "馆藏副本状态已被修改，请重试");
        }
        return copy;
    }

    /** 释放预留在架副本。 */
    @Transactional
    public void releaseHold(Long copyId) {
        copyRepository.findById(copyId).ifPresent(copy -> {
            copy.releaseHold();
            copyRepository.update(copy);
        });
    }
}
