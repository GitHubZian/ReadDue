package com.zian.backend.borrow.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 读者首页摘要的只读聚合查询。
 *
 * <p>四次计数合并成一条 SQL（条件聚合），避免首页一次请求打四次库；所有条件都以 readerId 过滤，
 * 调用方必须传当前登录读者，不允许从请求参数接收。
 */
public interface ReaderSummaryRepository {

    /**
     * 汇总一个读者的在借、临期、逾期与学期归还数。
     *
     * @param readerId  读者用户 ID
     * @param today     业务今天
     * @param plus3Days 今天 + 3 天（临期判定上界，含当天）
     * @param termStart 本学期起点（按归还时间比较）
     * @return 四项计数，无记录时为 0
     */
    Counts counts(long readerId, LocalDate today, LocalDate plus3Days, LocalDateTime termStart);

    /**
     * 四项计数。
     *
     * @param borrowed         在借册数
     * @param dueIn3Days       未来 3 天内到期册数
     * @param overdue          逾期未还册数
     * @param returnedThisTerm 本学期已归还册数
     */
    record Counts(long borrowed, long dueIn3Days, long overdue, long returnedThisTerm) {
    }
}
