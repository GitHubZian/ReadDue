package com.zian.backend.notify.interfaces;

import com.zian.backend.notify.application.NotifyInboxService;
import com.zian.backend.notify.interfaces.dto.ReaderNotifyDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者消息中心接口：站内信列表、未读数、标记已读。
 *
 * <p>数据边界由当前登录读者决定，请求体里没有 readerId，无法读取他人消息。
 */
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class ReaderNotifyController {

    private final NotifyInboxService notifyInboxService;

    /**
     * 查询当前读者的站内信，按投递时间倒序。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数，默认 20，上限 200
     * @return 站内信分页结果
     */
    @GetMapping
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<PageResult<ReaderNotifyDtos.NotificationItem>> list(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return ResultUtils.success(notifyInboxService.myNotifications(PageQuery.of(page, size)));
    }

    /**
     * 查询当前读者的未读站内信数。
     *
     * @return 未读条数
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<ReaderNotifyDtos.UnreadCount> unreadCount() {
        return ResultUtils.success(new ReaderNotifyDtos.UnreadCount(notifyInboxService.unreadCount()));
    }

    /**
     * 标记站内信已读：{@code all=true} 全部已读，否则按 {@code ids} 精确标记。
     *
     * @param request 标记范围
     * @return 实际更新条数
     */
    @PutMapping("/read")
    @PreAuthorize("hasAuthority('notify:read:self')")
    public BaseResponse<ReaderNotifyDtos.MarkReadResult> markRead(
            @RequestBody(required = false) ReaderNotifyDtos.MarkReadRequest request) {
        boolean all = request != null && Boolean.TRUE.equals(request.all());
        int updated = notifyInboxService.markRead(request == null ? null : request.ids(), all);
        return ResultUtils.success(new ReaderNotifyDtos.MarkReadResult(updated));
    }
}
