package com.zian.backend.catalog.domain.model;

/**
 * 书目库存快照（只读）。
 *
 * <p>来源视图 {@code v_book_stock}，未删除册与 {@code WAITING} 预约的唯一统计口径。
 *
 * @param totalCopies     馆藏总册数
 * @param availableCopies 在架可借册数
 * @param onLoanCopies    在借册数
 * @param queueLen        排队人数（WAITING）
 */
public record StockSnapshot(
        Integer totalCopies,
        Integer availableCopies,
        Integer onLoanCopies,
        Integer queueLen) {
}
