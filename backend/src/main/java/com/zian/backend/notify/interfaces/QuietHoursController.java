package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.QuietHoursService;
import com.zian.backend.notify.interfaces.dto.QuietHoursDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者免打扰时段：只能读写自己的窗口。
 */
@RestController
@RequestMapping("/v1/me/quiet-hours")
@RequiredArgsConstructor
public class QuietHoursController {

    private final QuietHoursService quietHoursService;

    /**
     * 读取当前读者的免打扰时段。
     *
     * @return 时段与来源（`CUSTOM` / `FALLBACK`）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<QuietHoursDtos.QuietHoursView> view() {
        return ResultUtils.success(quietHoursService.view());
    }

    /**
     * 局部更新免打扰时段。
     *
     * @param request 至少包含 enabled / start / end 之一
     * @return 更新后的时段
     */
    @PutMapping
    @PreAuthorize("hasAuthority('notify:pref:self')")
    public BaseResponse<QuietHoursDtos.QuietHoursView> update(
            @RequestBody QuietHoursDtos.UpdateRequest request) {
        return ResultUtils.success(quietHoursService.update(request));
    }
}
