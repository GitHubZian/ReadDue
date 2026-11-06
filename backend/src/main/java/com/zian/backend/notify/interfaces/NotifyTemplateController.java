package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.NotifyTemplateService;
import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知模板写接口（列表在 reporting）。
 */
@RestController
@RequestMapping("/v1/admin/templates")
@RequiredArgsConstructor
public class NotifyTemplateController {

    private final NotifyTemplateService templateService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<NotifyDtos.TemplateView> update(
            @PathVariable Long id, @RequestBody(required = false) NotifyDtos.TemplatePatch patch) {
        return ResultUtils.success(templateService.update(id, patch));
    }
}
