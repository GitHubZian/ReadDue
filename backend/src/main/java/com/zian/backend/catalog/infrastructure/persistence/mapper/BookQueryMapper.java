package com.zian.backend.catalog.infrastructure.persistence.mapper;

import com.zian.backend.catalog.domain.model.BookSummary;
import com.zian.backend.catalog.domain.model.CopyInfo;
import com.zian.backend.catalog.domain.model.MyReservation;
import com.zian.backend.catalog.domain.model.StockSnapshot;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 读者端书目检索的只读 SQL。
 *
 * <p>库存统计一律取自视图 {@code v_book_stock}，与看板/借还使用同一口径；结果行直接映射为
 * domain 的只读 record（列别名 = 组件名，构造器自动映射）。
 */
@Mapper
public interface BookQueryMapper {

    /**
     * 分页检索上架书目。
     *
     * @param keyword 已转义关键词，null 表示不过滤
     * @param offset  偏移量
     * @param limit   单页条数
     * @return 书目行
     */
    @Select("""
            <script>
            SELECT b.id, b.isbn13, b.title, b.subtitle, b.author, b.publisher,
                   b.category_code AS categoryCode, b.cover_url AS coverUrl,
                   COALESCE(s.total_copies, 0)     AS totalCopies,
                   COALESCE(s.available_copies, 0) AS availableCopies,
                   COALESCE(s.queue_len, 0)        AS queueLen
              FROM book b
              LEFT JOIN v_book_stock s ON s.book_id = b.id
             WHERE b.deleted = 0 AND b.status = 1
            <if test="keyword != null">
               AND (b.title LIKE CONCAT('%', #{keyword}, '%')
                    OR b.author LIKE CONCAT('%', #{keyword}, '%')
                    OR b.isbn13 = #{keyword})
            </if>
             ORDER BY b.id DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<BookSummary> search(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") int limit);

    /**
     * 统计命中关键词的上架书目数。
     *
     * @param keyword 已转义关键词，null 表示不过滤
     * @return 总条数
     */
    @Select("""
            <script>
            SELECT COUNT(*)
              FROM book b
             WHERE b.deleted = 0 AND b.status = 1
            <if test="keyword != null">
               AND (b.title LIKE CONCAT('%', #{keyword}, '%')
                    OR b.author LIKE CONCAT('%', #{keyword}, '%')
                    OR b.isbn13 = #{keyword})
            </if>
            </script>
            """)
    long count(@Param("keyword") String keyword);

    /**
     * 读取书目库存快照。
     *
     * @param bookId 书目 ID
     * @return 快照；视图无该行时各列为 null
     */
    @Select("""
            SELECT COALESCE(total_copies, 0)     AS totalCopies,
                   COALESCE(available_copies, 0) AS availableCopies,
                   COALESCE(on_loan_copies, 0)   AS onLoanCopies,
                   COALESCE(queue_len, 0)        AS queueLen
              FROM v_book_stock
             WHERE book_id = #{bookId}
            """)
    StockSnapshot stock(@Param("bookId") long bookId);

    /**
     * 查询书目全部未删除馆藏册：可借优先，其次在借，最后是损毁/遗失等不可借状态。
     *
     * @param bookId 书目 ID
     * @return 册列表
     */
    @Select("""
            SELECT id, barcode, location, status, acquired_at AS acquiredAt
              FROM book_copy
             WHERE book_id = #{bookId} AND deleted = 0
             ORDER BY FIELD(status, 'AVAILABLE', 'ON_LOAN', 'READY_SHELF', 'REPAIRING',
                            'DAMAGED', 'LOST', 'WITHDRAWN'),
                      barcode
            """)
    List<CopyInfo> copies(@Param("bookId") long bookId);

    /**
     * 查询读者在该书目上的有效预约（WAITING/READY）。
     *
     * @param bookId   书目 ID
     * @param readerId 读者 ID
     * @return 预约行；无则 null
     */
    @Select("""
            SELECT queue_no AS queueNo, status
              FROM reservation
             WHERE book_id = #{bookId} AND reader_id = #{readerId}
               AND status IN ('WAITING', 'READY')
             ORDER BY queue_no
             LIMIT 1
            """)
    MyReservation myReservation(@Param("bookId") long bookId, @Param("readerId") long readerId);
}
