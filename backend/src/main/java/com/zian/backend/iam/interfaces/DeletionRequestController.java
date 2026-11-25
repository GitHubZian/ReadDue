package com.zian.backend.iam.interfaces;

import com.zian.backend.iam.application.DeletionRequestService;
import com.zian.backend.iam.interfaces.dto.DeletionDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注销申请接口（M6）：读者提交/查询，管理员列表与批准/驳回。
 */
@RestController
@RequiredArgsConstructor
public class DeletionRequestController {

    private final DeletionRequestService service;

    /**
     * 提交注销申请。
     *
     * @param request 申请原因，可空
     * @return 申请视图
     */
    @PostMapping("/v1/me/deletion-request")
    @PreAuthorize("hasAuthority('privacy:self')")
    public BaseResponse<DeletionDtos.View> create(
            @Valid @RequestBody(required = false) DeletionDtos.CreateRequest request) {
        return ResultUtils.success(service.create(request));
    }

    /**
     * 当前读者最近一次注销申请。
     *
     * @return 申请视图；从未申请时为 null
     */
    @GetMapping("/v1/me/deletion-request")
    @PreAuthorize("hasAuthority('privacy:self')")
    public BaseResponse<DeletionDtos.View> mine() {
        return ResultUtils.success(service.mine());
    }

    /**
     * 管理端注销申请列表。
     *
     * @param status 状态过滤（PENDING/APPROVED/REJECTED），空表示全部
     * @param limit  条数上限，默认 50
     * @return 申请列表
     */
    @GetMapping("/v1/admin/deletion-requests")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<List<DeletionDtos.View>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        return ResultUtils.success(service.list(status, limit));
    }

    /**
     * 批准注销申请（复用立即遗忘脱敏与级联清理）。
     *
     * @param id 申请 ID
     * @return 处理后的申请
     */
    @PostMapping("/v1/admin/deletion-requests/{id}/approve")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<DeletionDtos.View> approve(@PathVariable long id) {
        return ResultUtils.success(service.approve(id));
    }

    /**
     * 驳回注销申请。
     *
     * @param id      申请 ID
     * @param request 处理备注，可空
     * @return 处理后的申请
     */
    @PostMapping("/v1/admin/deletion-requests/{id}/reject")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<DeletionDtos.View> reject(
            @PathVariable long id, @Valid @RequestBody(required = false) DeletionDtos.RejectRequest request) {
        return ResultUtils.success(service.reject(id, request));
    }
}
