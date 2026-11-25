package com.zian.backend.iam.interfaces.dto;

import java.util.List;

/**
 * 读者课表接口 DTO（M5-F）。
 *
 * <p>课表属于「场景声明」数据：关闭场景授权后宽限申请不再读取课表作为场景依据。
 */
public final class TimetableDtos {

    private TimetableDtos() {
    }

    /**
     * 一门课程。
     *
     * @param weekday   星期：1=周一 … 7=周日
     * @param startTime 开始时间（HH:mm）
     * @param endTime   结束时间（HH:mm）
     */
    public record CourseView(int weekday, String startTime, String endTime) {
    }

    /**
     * 覆盖式保存课表。
     *
     * @param courses 课程列表；空列表表示清空课表
     */
    public record TimetableRequest(List<CourseView> courses) {
    }

    /**
     * 课表结果。
     *
     * @param confirmed 是否已确认（至少一门课程）
     * @param courses   课程列表
     * @param updatedAt 最近更新时间（+08:00）；从未保存过则为 null
     */
    public record TimetableResult(boolean confirmed, List<CourseView> courses, String updatedAt) {
    }

    /**
     * 静默时段（课程前后各放宽 15 分钟，重叠自动合并）。
     *
     * @param startTime 开始时间（HH:mm）
     * @param endTime   结束时间（HH:mm）
     */
    public record QuietWindow(String startTime, String endTime) {
    }

    /**
     * 指定日期的静默时段。
     *
     * @param date    日期（yyyy-MM-dd）
     * @param windows 当天的静默时段，按时间升序
     */
    public record QuietWindowsView(String date, List<QuietWindow> windows) {
    }
}
