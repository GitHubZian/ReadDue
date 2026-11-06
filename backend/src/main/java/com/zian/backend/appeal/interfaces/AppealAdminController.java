package com.zian.backend.appeal.interfaces;

import com.zian.backend.appeal.application.AppealService;
import com.zian.backend.appeal.interfaces.dto.AppealDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 申诉处理写接口。
 */
@RestController
@RequestMapping("/v1/admin/appeals")
@RequiredArgsConstructor
public class AppealAdminController {

    private final AppealService appealService;

    @PostMapping("/{id}/ack")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> ack(@PathVariable Long id) {
        return ResultUtils.success(appealService.ack(id));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> resolve(
            @PathVariable Long id, @RequestBody(required = false) AppealDtos.ActionRequest request) {
        return ResultUtils.success(appealService.resolve(id, request));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> reject(
            @PathVariable Long id, @RequestBody(required = false) AppealDtos.ActionRequest request) {
        return ResultUtils.success(appealService.reject(id, request));
    }

    @PostMapping("/{id}/compensate")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> compensate(
            @PathVariable Long id, @RequestBody(required = false) AppealDtos.ActionRequest request) {
        return ResultUtils.success(appealService.compensate(id, request));
    }
}
