package com.zian.backend.borrow.domain.model;

/**
 * 读者首页摘要：在借、临期、逾期、配额与信用档位的只读快照。
 *
 * <p>所有计数都以当前读者为边界，由 {@code borrow_order} 与 {@code reader_credit} 聚合得到，
 * 不包含预约数与消息数（分别来自预约与通知上下文）。
 *
 * @param borrowed        当前在借（BORROWED/OVERDUE）册数
 * @param dueIn3Days      未来 3 天内到期（含今天）的在借册数
 * @param overdue         已逾期未还册数
 * @param quota           同时在借上限（册），来自规则参数 BORROW.MAX_ACTIVE
 * @param returnedThisTerm 本学期已归还册数（学期起点见 ReaderSummaryService）
 * @param creditLevel     信用档位 0–3（0 最优）
 * @param creditLabel     信用档位展示名
 */
public record ReaderSummary(
        int borrowed,
        int dueIn3Days,
        int overdue,
        int quota,
        int returnedThisTerm,
        int creditLevel,
        String creditLabel) {
}
