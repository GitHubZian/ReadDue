package com.zian.backend.iam.domain.model;

import java.time.LocalTime;

/**
 * 读者课表中的一门课程（M5-F）。
 *
 * <p>只描述周期性的上课时间，不绑定具体教学周；用于推算「静默时段」。
 *
 * @param weekday   星期：1=周一 … 7=周日
 * @param startTime 开始时间
 * @param endTime   结束时间
 */
public record Course(int weekday, LocalTime startTime, LocalTime endTime) {
}
