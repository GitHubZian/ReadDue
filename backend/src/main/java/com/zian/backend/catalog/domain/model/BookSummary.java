package com.zian.backend.catalog.domain.model;

/**
 * 书目检索结果行（只读）。
 *
 * <p>字段名与 {@code BookQueryMapper.search} 的列别名一一对应，供 MyBatis 构造器映射使用；
 * 库存与排队数来自视图 {@code v_book_stock}，只统计未删除的册与 {@code WAITING} 预约。
 *
 * @param id              书目 ID
 * @param isbn13          ISBN-13
 * @param title           书名
 * @param subtitle        副标题
 * @param author          作者
 * @param publisher       出版社
 * @param categoryCode    中图法分类号
 * @param coverUrl        封面地址
 * @param totalCopies     馆藏总册数
 * @param availableCopies 在架可借册数
 * @param queueLen        排队人数（WAITING）
 */
public record BookSummary(
        Long id,
        String isbn13,
        String title,
        String subtitle,
        String author,
        String publisher,
        String categoryCode,
        String coverUrl,
        Integer totalCopies,
        Integer availableCopies,
        Integer queueLen) {

    /**
     * 总册数，null 视为 0。
     *
     * @return 馆藏总册数
     */
    public int total() {
        return totalCopies == null ? 0 : totalCopies;
    }

    /**
     * 可借册数，null 视为 0。
     *
     * @return 在架可借册数
     */
    public int available() {
        return availableCopies == null ? 0 : availableCopies;
    }

    /**
     * 排队人数，null 视为 0。
     *
     * @return 排队人数
     */
    public int waiting() {
        return queueLen == null ? 0 : queueLen;
    }
}
