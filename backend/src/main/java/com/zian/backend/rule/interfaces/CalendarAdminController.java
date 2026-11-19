package com.zian.backend.rule.interfaces;

import com.zian.backend.rule.application.AcademicCalendarService;
import com.zian.backend.rule.interfaces.dto.CalendarDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 校历写接口（只读列表见 GET /v1/admin/calendar）。
 */
@RestController
@RequestMapping("/v1/admin/calendar")
@RequiredArgsConstructor
public class CalendarAdminController {

    private final AcademicCalendarService calendarService;

    /**
     * 新增校历区间。
     *
     * @param request 学期、场景、起止日期
     * @return 新建后的区间
     */
    @PostMapping
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<CalendarDtos.CalendarView> create(@RequestBody @Valid CalendarDtos.SaveRequest request) {
        return ResultUtils.success(calendarService.create(request));
    }

    /**
     * 修改校历区间。
     *
     * @param id      区间主键
     * @param request 新值
     * @return 修改后的区间
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<CalendarDtos.CalendarView> update(
            @PathVariable long id, @RequestBody @Valid CalendarDtos.SaveRequest request) {
        return ResultUtils.success(calendarService.update(id, request));
    }

    /**
     * 软删除校历区间。
     *
     * @param id 区间主键
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<Void> delete(@PathVariable long id) {
        calendarService.delete(id);
        return ResultUtils.success(null);
    }
}
