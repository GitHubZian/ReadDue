package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.ReaderNotifyPreferenceService;
import com.zian.backend.notify.interfaces.dto.NotifyPreferenceDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者通知偏好：主题字典、差异偏好、批量设置与变更日志。
 */
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class ReaderNotifyPreferenceController {

    private final ReaderNotifyPreferenceService preferenceService;

    /**
     * 全部上线主题及当前生效偏好。
     *
     * @return 主题列表
     */
    @GetMapping("/notify-topics")
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<List<NotifyPreferenceDtos.TopicView>> topics() {
        return ResultUtils.success(preferenceService.topics());
    }

    /**
     * 读者自定义的偏好（只含与默认不同的主题）。
     *
     * @return 差异列表
     */
    @GetMapping("/notify-preferences")
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<List<NotifyPreferenceDtos.PreferenceView>> preferences() {
        return ResultUtils.success(preferenceService.preferences());
    }

    /**
     * 批量设置偏好；与默认相同会删除差异行。
     *
     * @param request 待更新项
     * @return 变更条数与最新差异
     */
    @PutMapping("/notify-preferences")
    @PreAuthorize("hasAuthority('notify:pref:self')")
    public BaseResponse<NotifyPreferenceDtos.UpdateResult> update(
            @RequestBody NotifyPreferenceDtos.UpdateRequest request) {
        return ResultUtils.success(preferenceService.update(request));
    }

    /**
     * 最近的偏好变更日志。
     *
     * @param limit 条数，1–100，默认 20
     * @return 按时间倒序
     */
    @GetMapping("/notify-preferences/log")
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<List<NotifyPreferenceDtos.LogView>> logs(
            @RequestParam(required = false) Integer limit) {
        return ResultUtils.success(preferenceService.logs(limit));
    }
}
