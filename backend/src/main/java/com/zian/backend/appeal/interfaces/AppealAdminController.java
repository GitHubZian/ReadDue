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

    /**
     * 受理申诉工单：记录首次回应时间，状态进入处理中。
     *
     * @param id 工单 ID
     * @return 工单当前状态与补偿天数
     */
    @PostMapping("/{id}/ack")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> ack(@PathVariable Long id) {
        return ResultUtils.success(appealService.ack(id));
    }

    /**
     * 出具申诉结论：按核查结果撤销逾期或维持原判，并记录处理说明。
     *
     * @param id      工单 ID
     * @param request 处理说明与可选补偿天数
     * @return 工单当前状态与补偿天数
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> resolve(
            @PathVariable Long id, @RequestBody(required = false) AppealDtos.ActionRequest request) {
        return ResultUtils.success(appealService.resolve(id, request));
    }

    /**
     * 驳回申诉工单并记录理由。
     *
     * @param id      工单 ID
     * @param request 驳回说明
     * @return 工单当前状态
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> reject(
            @PathVariable Long id, @RequestBody(required = false) AppealDtos.ActionRequest request) {
        return ResultUtils.success(appealService.reject(id, request));
    }

    /**
     * 对申诉工单执行补偿：补还逾期天数，用于超时未回应或误判场景。
     *
     * @param id      工单 ID
     * @param request 补偿天数与说明
     * @return 工单当前状态与补偿天数
     */
    @PostMapping("/{id}/compensate")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public BaseResponse<AppealDtos.AppealActionView> compensate(
            @PathVariable Long id, @RequestBody(required = false) AppealDtos.ActionRequest request) {
        return ResultUtils.success(appealService.compensate(id, request));
    }
}
