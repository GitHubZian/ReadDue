package com.zian.backend.iam.application;

import com.zian.backend.iam.domain.model.Course;
import com.zian.backend.iam.domain.repository.TimetableRepository;
import com.zian.backend.iam.interfaces.dto.TimetableDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者课表与静默时段（M5-F）。
 *
 * <p>课表上限 30 门；同一天课程不允许时间重叠。静默时段 = 每门课前后各放宽 15 分钟，
 * 重叠区间自动合并。
 */
@Service
@RequiredArgsConstructor
public class TimetableService {

    /** 课表容量上限。 */
    private static final int MAX_COURSES = 30;

    /** 静默时段相对课程前后各放宽的分钟数。 */
    private static final int QUIET_PADDING_MINUTES = 15;

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final TimetableRepository timetableRepository;

    /**
     * 读取当前读者课表。
     *
     * @return 课表结果；没有任何课程时 confirmed=false
     */
    public TimetableDtos.TimetableResult get() {
        long userId = UserContext.require().id();
        return toResult(timetableRepository.findActive(userId), timetableRepository.lastUpdated(userId));
    }

    /**
     * 覆盖式保存当前读者课表。
     *
     * @param request 课程列表；null 或空列表表示清空
     * @return 保存后的课表结果（confirmed=true 当且仅当有课程）
     */
    @Transactional
    public TimetableDtos.TimetableResult replace(TimetableDtos.TimetableRequest request) {
        long userId = UserContext.require().id();
        List<TimetableDtos.CourseView> views =
                request == null || request.courses() == null ? List.of() : request.courses();
        if (views.size() > MAX_COURSES) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课表最多 " + MAX_COURSES + " 门课程");
        }
        List<Course> courses = views.stream().map(this::toCourse).toList();
        assertNoOverlap(courses);
        timetableRepository.replace(userId, courses);
        return toResult(courses, timetableRepository.lastUpdated(userId));
    }

    /**
     * 指定日期的静默时段。
     *
     * @param date 日期（yyyy-MM-dd）
     * @return 当天的静默时段，已排序并合并重叠区间
     */
    public TimetableDtos.QuietWindowsView quietWindows(String date) {
        long userId = UserContext.require().id();
        LocalDate day;
        try {
            day = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期格式应为 yyyy-MM-dd");
        }
        int weekday = day.getDayOfWeek().getValue();
        List<Course> courses = timetableRepository.findActive(userId).stream()
                .filter(course -> course.weekday() == weekday)
                .sorted(Comparator.comparing(Course::startTime))
                .toList();
        List<TimetableDtos.QuietWindow> windows = merge(courses);
        return new TimetableDtos.QuietWindowsView(day.toString(), windows);
    }

    private TimetableDtos.TimetableResult toResult(List<Course> courses, java.time.LocalDateTime updatedAt) {
        List<TimetableDtos.CourseView> views = courses.stream()
                .sorted(Comparator.comparingInt(Course::weekday).thenComparing(Course::startTime))
                .map(course -> new TimetableDtos.CourseView(
                        course.weekday(),
                        course.startTime().format(HH_MM),
                        course.endTime().format(HH_MM)))
                .toList();
        return new TimetableDtos.TimetableResult(!views.isEmpty(), views, Times.iso(updatedAt));
    }

    private Course toCourse(TimetableDtos.CourseView view) {
        if (view == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程不能为空");
        }
        if (view.weekday() < 1 || view.weekday() > 7) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星期必须为 1-7");
        }
        LocalTime start = parseTime(view.startTime(), "开始时间");
        LocalTime end = parseTime(view.endTime(), "结束时间");
        if (!start.isBefore(end)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程结束时间必须晚于开始时间");
        }
        return new Course(view.weekday(), start, end);
    }

    private LocalTime parseTime(String raw, String label) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, label + "不能为空");
        }
        try {
            return LocalTime.parse(raw.trim(), HH_MM);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, label + "格式应为 HH:mm");
        }
    }

    private void assertNoOverlap(List<Course> courses) {
        for (int weekday = 1; weekday <= 7; weekday++) {
            final int day = weekday;
            List<Course> sameDay = new ArrayList<>(courses.stream()
                    .filter(course -> course.weekday() == day)
                    .sorted(Comparator.comparing(Course::startTime))
                    .toList());
            for (int i = 1; i < sameDay.size(); i++) {
                Course previous = sameDay.get(i - 1);
                Course current = sameDay.get(i);
                // 首尾相接（10:00-11:00 与 11:00-12:00）不算重叠。
                if (current.startTime().isBefore(previous.endTime())) {
                    throw new BusinessException(
                            ErrorCode.RULE_VIOLATION, "同一天的课程时间不能重叠");
                }
            }
        }
    }

    private List<TimetableDtos.QuietWindow> merge(List<Course> courses) {
        List<TimetableDtos.QuietWindow> windows = new ArrayList<>();
        LocalTime dayStart = LocalTime.MIN;
        LocalTime dayEnd = LocalTime.of(23, 59);
        for (Course course : courses) {
            LocalTime start = course.startTime().minusMinutes(QUIET_PADDING_MINUTES);
            if (start.isBefore(dayStart)) {
                start = dayStart;
            }
            LocalTime end = course.endTime().plusMinutes(QUIET_PADDING_MINUTES);
            if (end.isAfter(dayEnd)) {
                end = dayEnd;
            }
            if (!windows.isEmpty()) {
                TimetableDtos.QuietWindow last = windows.get(windows.size() - 1);
                LocalTime lastEnd = LocalTime.parse(last.endTime(), HH_MM);
                if (!start.isAfter(lastEnd)) {
                    if (end.isAfter(lastEnd)) {
                        windows.set(windows.size() - 1,
                                new TimetableDtos.QuietWindow(last.startTime(), end.format(HH_MM)));
                    }
                    continue;
                }
            }
            windows.add(new TimetableDtos.QuietWindow(start.format(HH_MM), end.format(HH_MM)));
        }
        return windows;
    }
}
