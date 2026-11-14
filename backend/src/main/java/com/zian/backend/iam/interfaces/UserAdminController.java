package com.zian.backend.iam.interfaces;

import com.zian.backend.iam.application.UserAdminService;
import com.zian.backend.iam.interfaces.dto.AuthDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号治理写接口（列表读模型在 reporting）。
 */
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    /**
     * 拉黑账号：阻止借出、续借、预约与申请宽限，归还和申诉不受影响。
     *
     * @param id      用户 ID
     * @param request 拉黑原因与可选到期时间
     * @return 用户状态
     */
    @PostMapping("/{id}/blacklist")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<AuthDtos.UserStatusView> blacklist(
            @PathVariable Long id, @RequestBody @Valid AuthDtos.BlacklistRequest request) {
        return ResultUtils.success(userAdminService.blacklist(id, request));
    }

    /**
     * 解除拉黑，恢复账号为正常状态。
     *
     * @param id 用户 ID
     * @return 用户状态
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('user:blacklist')")
    public BaseResponse<AuthDtos.UserStatusView> restore(@PathVariable Long id) {
        return ResultUtils.success(userAdminService.restore(id));
    }
}
