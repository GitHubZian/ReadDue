package com.zian.backend.catalog.domain.repository;

import com.zian.backend.catalog.domain.model.BookDetail;
import com.zian.backend.catalog.domain.model.BookSummary;
import com.zian.backend.catalog.domain.model.CopyInfo;
import com.zian.backend.catalog.domain.model.MyReservation;
import com.zian.backend.catalog.domain.model.StockSnapshot;
import java.util.List;
import java.util.Optional;

/**
 * 书目只读查询端口（读者端检索与馆藏）。
 *
 * <p>与 {@link BookRepository} 区分：后者服务借还流程的强一致读取，本端口只做展示型查询，
 * 允许使用视图与联表，不参与写事务。
 */
public interface BookQueryRepository {

    /**
     * 按关键词分页检索上架书目，关键词匹配书名/作者（模糊）或 ISBN（精确）。
     *
     * @param keyword 已转义的关键词；null 表示不过滤
     * @param offset  偏移量
     * @param limit   单页条数
     * @return 检索结果行
     */
    List<BookSummary> search(String keyword, long offset, int limit);

    /**
     * 统计命中关键词的上架书目数。
     *
     * @param keyword 已转义的关键词；null 表示不过滤
     * @return 总条数
     */
    long count(String keyword);

    /**
     * 查询书目详情（含库存）。
     *
     * @param bookId 书目 ID
     * @return 上架且未删除的书目；否则空
     */
    Optional<BookDetail> findDetail(long bookId);

    /**
     * 查询书目的全部未删除馆藏册。
     *
     * @param bookId 书目 ID
     * @return 册列表（可借优先）
     */
    List<CopyInfo> copies(long bookId);

    /**
     * 查询书目的库存快照。
     *
     * @param bookId 书目 ID
     * @return 库存快照；书目不存在时各项为 0
     */
    StockSnapshot stock(long bookId);

    /**
     * 查询读者在该书目上的有效预约（WAITING/READY）。
     *
     * @param bookId   书目 ID
     * @param readerId 读者 ID
     * @return 有效预约；无则空
     */
    Optional<MyReservation> myReservation(long bookId, long readerId);
}
