package com.zian.backend.iam.interfaces;

import com.zian.backend.iam.application.TimetableService;
import com.zian.backend.iam.interfaces.dto.TimetableDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者课表接口（M5-F）：课表维护与静默时段查询。
 */
@RestController
@RequestMapping("/v1/me/timetable")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TimetableController {

    private final TimetableService timetableService;

    /**
     * 读取当前读者课表。
     *
     * @return 课表结果
     */
    @GetMapping
    public BaseResponse<TimetableDtos.TimetableResult> get() {
        return ResultUtils.success(timetableService.get());
    }

    /**
     * 覆盖式保存当前读者课表。
     *
     * @param request 课程列表；空列表表示清空
     * @return 保存后的课表结果
     */
    @PutMapping
    public BaseResponse<TimetableDtos.TimetableResult> replace(
            @RequestBody(required = false) TimetableDtos.TimetableRequest request) {
        return ResultUtils.success(timetableService.replace(request));
    }

    /**
     * 指定日期的静默时段。
     *
     * @param date 日期（yyyy-MM-dd）
     * @return 当天的静默时段
     */
    @GetMapping("/quiet-windows")
    public BaseResponse<TimetableDtos.QuietWindowsView> quietWindows(@RequestParam String date) {
        return ResultUtils.success(timetableService.quietWindows(date));
    }
}
