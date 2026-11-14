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

    /**
     * 审批通过宽限：写入实批天数并顺延借阅单的有效应还日期。
     *
     * @param id      宽限申请 ID
     * @param request 实批天数（缺省取计算值）与备注
     * @return 审批后的状态与实批天数
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<GraceDtos.GraceDecisionView> approve(
            @PathVariable Long id, @RequestBody(required = false) GraceDtos.ApproveRequest request) {
        return ResultUtils.success(graceService.approve(id, request));
    }

    /**
     * 驳回宽限申请并记录理由。
     *
     * @param id      宽限申请 ID
     * @param request 驳回说明
     * @return 审批后的状态
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('grace:approve')")
    public BaseResponse<GraceDtos.GraceDecisionView> reject(
            @PathVariable Long id, @RequestBody(required = false) GraceDtos.RejectRequest request) {
        return ResultUtils.success(graceService.reject(id, request));
    }
}
