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

    /**
     * 按条码取副本，不存在时抛 404。
     *
     * @param barcode 副本条码
     * @return 副本聚合
     */
    @Transactional(readOnly = true)
    public BookCopy requireCopyByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "条码不能为空");
        }
        return copyRepository
                .findByBarcode(barcode.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到条码为 " + barcode + " 的馆藏"));
    }

    /**
     * 按 ID 取书目，不存在时抛 404。
     *
     * @param bookId 书目 ID
     * @return 书目
     */
    @Transactional(readOnly = true)
    public Book requireBook(Long bookId) {
        return bookRepository
                .findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "书目不存在"));
    }

    /**
     * 批量取书目并按下标建立映射，避免 N+1。
     *
     * @param ids 书目 ID 集合
     * @return ID 到书目的映射
     */
    @Transactional(readOnly = true)
    public Map<Long, Book> booksByIds(Collection<Long> ids) {
        return bookRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(Book::id, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量取书目。
     *
     * @param ids 书目 ID 集合
     * @return 书目列表
     */
    @Transactional(readOnly = true)
    public List<Book> books(Collection<Long> ids) {
        return bookRepository.findByIds(ids);
    }

    /**
     * 判断书目是否有可借副本，用于预约前的拦截。
     *
     * @param bookId 书目 ID
     * @return 存在可借副本时为 true
     */
    @Transactional(readOnly = true)
    public boolean hasAvailable(Long bookId) {
        return copyRepository.findFirstAvailable(bookId).isPresent();
    }

    /**
     * 借出副本：AVAILABLE 置为 ON_LOAN 并递增版本。
     *
     * @param copyId 副本 ID
     * @return 更新后的副本
     */
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

    /**
     * 归还副本：ON_LOAN 置为 AVAILABLE 或 DAMAGED。
     *
     * @param copyId  副本 ID
     * @param damaged 是否损坏
     * @return 更新后的副本
     */
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

    /**
     * 为预约锁定一本可借副本（置为 READY_SHELF）。
     *
     * @param bookId 书目 ID
     * @return 被锁定的副本
     */
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

    /**
     * 释放预约占位，副本回到可借状态。
     *
     * @param copyId 副本 ID
     */
    @Transactional
    public void releaseHold(Long copyId) {
        copyRepository.findById(copyId).ifPresent(copy -> {
            copy.releaseHold();
            copyRepository.update(copy);
        });
    }

    /**
     * 释放某书目第一本被预约占位的副本（预约过期/取消时调用）。
     *
     * @param bookId 书目 ID
     */
    @Transactional
    public void releaseHoldForBook(Long bookId) {
        copyRepository.findFirstHeld(bookId).ifPresent(copy -> {
            copy.releaseHold();
            copyRepository.update(copy);
        });
    }
}
