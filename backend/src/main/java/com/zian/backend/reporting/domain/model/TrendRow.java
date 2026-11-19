package com.zian.backend.reporting.domain.model;

import java.time.LocalDate;

/**
 * 借还趋势行：按自然日聚合的借出量或归还量。
 *
 * <p>对应 SQL 结果列 {@code day}（DATE）与 {@code total}（COUNT）。
 *
 * @param day   统计日期
 * @param total 当日借出或归还册数
 */
public record TrendRow(LocalDate day, Long total) {
}
