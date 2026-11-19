package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.NotifyTopicService;
import com.zian.backend.notify.interfaces.dto.NotifyTopicDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端通知主题字典：查看与调整组织默认值。
 */
@RestController
@RequestMapping("/v1/admin/notify-topics")
@RequiredArgsConstructor
public class NotifyTopicAdminController {

    private final NotifyTopicService topicService;

    /**
     * 全部主题（含下线）。
     *
     * @return 主题列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<List<NotifyTopicDtos.AdminTopicView>> list() {
        return ResultUtils.success(topicService.list());
    }

    /**
     * 更新主题的组织默认值。
     *
     * @param code 主题编码
     * @param request 新默认值，字段为空表示保持不变
     * @return 更新后的主题
     */
    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<NotifyTopicDtos.AdminTopicView> updateDefaults(
            @PathVariable String code, @RequestBody NotifyTopicDtos.UpdateDefaultsRequest request) {
        return ResultUtils.success(topicService.updateDefaults(code, request));
    }
}
