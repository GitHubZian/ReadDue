package com.zian.backend.catalog.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 书目详情（只读）。
 *
 * <p>由书目主表与库存视图拼装：标签保留为字符串列表，价格以 {@link java.math.BigDecimal} 返回，
 * 由接口层决定输出格式（本项目金额统一输出字符串，避免浮点误差）。
 *
 * @param id              书目 ID
 * @param isbn13          ISBN-13
 * @param title           书名
 * @param subtitle        副标题
 * @param author          作者
 * @param publisher       出版社
 * @param publishDate     出版日期
 * @param categoryCode    中图法分类号
 * @param language        语种
 * @param price           定价
 * @param summary         简介
 * @param coverUrl        封面地址
 * @param tags            标签
 * @param onShelf         是否上架
 * @param totalCopies     馆藏总册数
 * @param availableCopies 在架可借册数
 * @param onLoanCopies    在借册数
 * @param queueLen        排队人数（WAITING）
 */
public record BookDetail(
        Long id,
        String isbn13,
        String title,
        String subtitle,
        String author,
        String publisher,
        LocalDate publishDate,
        String categoryCode,
        String language,
        BigDecimal price,
        String summary,
        String coverUrl,
        List<String> tags,
        boolean onShelf,
        int totalCopies,
        int availableCopies,
        int onLoanCopies,
        int queueLen) {
}
