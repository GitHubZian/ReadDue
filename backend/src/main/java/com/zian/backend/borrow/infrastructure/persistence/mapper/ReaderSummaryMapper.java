package com.zian.backend.borrow.infrastructure.persistence.mapper;

import com.zian.backend.borrow.domain.repository.ReaderSummaryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 读者首页摘要的聚合查询。
 */
@Mapper
public interface ReaderSummaryMapper {

    /**
     * 一条 SQL 汇总在借、三天内到期、逾期与学期归还四项计数。
     *
     * @param readerId  读者用户 ID
     * @param today     业务今天
     * @param plus3Days 今天 + 3 天
     * @param termStart 学期起点
     * @return 计数行
     */
    @Select("""
            SELECT
              COALESCE(SUM(CASE WHEN status IN ('BORROWED','OVERDUE') THEN 1 ELSE 0 END), 0) AS borrowed,
              COALESCE(SUM(CASE WHEN status IN ('BORROWED','OVERDUE')
                                 AND effective_due_at BETWEEN #{today} AND #{plus3Days}
                           THEN 1 ELSE 0 END), 0) AS dueIn3Days,
              COALESCE(SUM(CASE WHEN status IN ('BORROWED','OVERDUE')
                                 AND effective_due_at < #{today}
                           THEN 1 ELSE 0 END), 0) AS overdue,
              COALESCE(SUM(CASE WHEN status = 'RETURNED' AND returned_at >= #{termStart}
                           THEN 1 ELSE 0 END), 0) AS returnedThisTerm
            FROM borrow_order
            WHERE reader_id = #{readerId}
            """)
    ReaderSummaryRepository.Counts counts(
            @Param("readerId") long readerId,
            @Param("today") LocalDate today,
            @Param("plus3Days") LocalDate plus3Days,
            @Param("termStart") LocalDateTime termStart);
}
