package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.NotifyService;
import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知任务与催还写接口。
 */
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class NotifyAdminController {

    private final NotifyService notifyService;

    @PostMapping("/notify-tasks/{id}/retry")
    @PreAuthorize("hasAuthority('notify:manage')")
    public BaseResponse<Void> retry(@PathVariable Long id) {
        notifyService.retry(id);
        return ResultUtils.success(null);
    }

    @PostMapping("/remind/batch")
    @PreAuthorize("hasAuthority('overdue:board')")
    public BaseResponse<NotifyDtos.RemindResult> remind(@RequestBody(required = false) NotifyDtos.RemindRequest request) {
        List<Long> ids = request == null || request.ids() == null
                ? List.of()
                : request.ids().stream().map(this::parseId).toList();
        return ResultUtils.success(new NotifyDtos.RemindResult(notifyService.remindBatch(ids)));
    }

    private Long parseId(String raw) {
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException e) {
            throw new com.zian.backend.shared.exception.BusinessException(
                    com.zian.backend.shared.common.ErrorCode.PARAMS_ERROR, "订单 id 格式不正确");
        }
    }
}
