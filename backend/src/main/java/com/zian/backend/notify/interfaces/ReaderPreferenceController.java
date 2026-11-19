package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.ReaderPreferenceService;
import com.zian.backend.notify.interfaces.dto.ReaderPreferenceDtos;
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
 * 读者渠道开关：只能读写自己的偏好。
 */
@RestController
@RequestMapping("/v1/me/channels")
@RequiredArgsConstructor
public class ReaderPreferenceController {

    private final ReaderPreferenceService readerPreferenceService;

    /**
     * 读取当前读者的渠道开关。
     *
     * @return 邮件与站内信开关
     */
    @GetMapping
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<ReaderPreferenceDtos.ChannelsView> channels() {
        return ResultUtils.success(readerPreferenceService.channels());
    }

    /**
     * 局部更新渠道开关。
     *
     * @param request 至少包含 email / inbox 之一
     * @return 更新条数与最新开关
     */
    @PutMapping
    @PreAuthorize("hasAuthority('notify:pref:self')")
    public BaseResponse<ReaderPreferenceDtos.UpdateResult> update(
            @RequestBody ReaderPreferenceDtos.UpdateRequest request) {
        return ResultUtils.success(readerPreferenceService.update(request));
    }
}
