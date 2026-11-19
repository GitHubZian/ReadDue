package com.zian.backend.catalog.interfaces.dto;

import java.util.List;

/**
 * 读者端书目 DTO。
 */
public final class BookDtos {

    private BookDtos() {
    }

    /**
     * 检索结果项。
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
     * @param queueLen        排队人数
     */
    public record BookItem(
            Long id,
            String isbn13,
            String title,
            String subtitle,
            String author,
            String publisher,
            String categoryCode,
            String coverUrl,
            int totalCopies,
            int availableCopies,
            int queueLen) {
    }

    /**
     * 书目详情。
     *
     * @param id              书目 ID
     * @param isbn13          ISBN-13
     * @param title           书名
     * @param subtitle        副标题
     * @param author          作者
     * @param publisher       出版社
     * @param publishDate     出版日期（yyyy-MM-dd）
     * @param categoryCode    中图法分类号
     * @param language        语种
     * @param price           定价（元，字符串）
     * @param summary         简介
     * @param coverUrl        封面地址
     * @param tags            标签
     * @param onShelf         是否上架
     * @param totalCopies     馆藏总册数
     * @param availableCopies 在架可借册数
     * @param onLoanCopies    在借册数
     * @param queueLen        排队人数
     */
    public record BookDetail(
            Long id,
            String isbn13,
            String title,
            String subtitle,
            String author,
            String publisher,
            String publishDate,
            String categoryCode,
            String language,
            String price,
            String summary,
            String coverUrl,
            List<String> tags,
            boolean onShelf,
            int totalCopies,
            int availableCopies,
            int onLoanCopies,
            int queueLen) {
    }

    /**
     * 馆藏册。
     *
     * @param id         册 ID
     * @param barcode    馆藏条码
     * @param location   馆藏位置
     * @param status     册状态
     * @param acquiredAt 入藏日期（yyyy-MM-dd）
     */
    public record CopyItem(Long id, String barcode, String location, String status, String acquiredAt) {
    }

    /**
     * 书目预约队列视图。
     *
     * @param bookId   书目 ID
     * @param waiting  当前排队人数
     * @param myQueueNo 本人排队序号；未预约时为 null
     * @param myStatus  本人预约状态（WAITING/READY）；未预约时为 null
     */
    public record QueueView(Long bookId, int waiting, Integer myQueueNo, String myStatus) {
    }
}
