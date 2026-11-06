package com.zian.backend.grace.interfaces;

import com.zian.backend.grace.application.GraceService;
import com.zian.backend.grace.interfaces.dto.GraceDtos;
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
 * 宽限审批写接口。
 */
@RestController
@RequestMapping("/v1/admin/graces")
@RequiredArgsConstructor
public class GraceAdminController {

    private final GraceService graceService;

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<GraceDtos.GraceDecisionView> approve(
            @PathVariable Long id, @RequestBody(required = false) GraceDtos.ApproveRequest request) {
        return ResultUtils.success(graceService.approve(id, request));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<GraceDtos.GraceDecisionView> reject(
            @PathVariable Long id, @RequestBody(required = false) GraceDtos.RejectRequest request) {
        return ResultUtils.success(graceService.reject(id, request));
    }
}
