package com.zian.backend.iam.interfaces;

import com.zian.backend.iam.application.PrivacyService;
import com.zian.backend.iam.interfaces.dto.PrivacyDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者隐私：授权开关与「立即遗忘」。
 */
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class PrivacyController {

    private final PrivacyService privacyService;

    /**
     * 读取当前读者的授权状态。
     *
     * @return 授权视图
     */
    @GetMapping("/consents")
    @PreAuthorize("hasAuthority('privacy:self')")
    public BaseResponse<PrivacyDtos.ConsentsView> consents() {
        return ResultUtils.success(privacyService.consents());
    }

    /**
     * 局部更新授权。
     *
     * @param request 至少包含 sceneConsent / cacheConsent 之一
     * @return 更新后的授权视图
     */
    @PutMapping("/consents")
    @PreAuthorize("hasAuthority('privacy:self')")
    public BaseResponse<PrivacyDtos.ConsentsView> update(
            @RequestBody PrivacyDtos.UpdateRequest request) {
        return ResultUtils.success(privacyService.update(request));
    }

    /**
     * 立即遗忘：脱敏个人信息，保留借阅等事实并写审计。
     *
     * @return 脱敏结果
     */
    @PostMapping("/forget")
    @PreAuthorize("hasAuthority('privacy:self')")
    public BaseResponse<PrivacyDtos.ForgetResult> forget() {
        return ResultUtils.success(privacyService.forget());
    }
}
