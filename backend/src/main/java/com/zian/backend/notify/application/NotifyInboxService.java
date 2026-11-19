package com.zian.backend.notify.application;

import com.zian.backend.notify.domain.model.InboxMessage;
import com.zian.backend.notify.domain.repository.NotifyInboxRepository;
import com.zian.backend.notify.interfaces.dto.ReaderNotifyDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者消息中心：只读列表、未读数与标记已读。
 *
 * <p>所有查询都以当前登录读者为边界（{@link UserContext#requireId()}），不提供按 ID 读他人消息的入口。
 */
@Service
@RequiredArgsConstructor
public class NotifyInboxService {

    /** 单次标记已读的 ID 上限，避免超长 IN 列表。 */
    private static final int MARK_READ_LIMIT = 200;

    private final NotifyInboxRepository inboxRepository;

    /**
     * 分页查询当前读者的站内信，按投递时间倒序。
     *
     * @param query 分页参数
     * @return 站内信分页结果
     */
    public PageResult<ReaderNotifyDtos.NotificationItem> myNotifications(PageQuery query) {
        long readerId = UserContext.requireId();
        long total = inboxRepository.count(readerId);
        if (total == 0) {
            return PageResult.empty(query);
        }
        List<ReaderNotifyDtos.NotificationItem> items = inboxRepository.page(readerId, query.offset(), query.size())
                .stream()
                .map(NotifyInboxService::toItem)
                .toList();
        return PageResult.of(items, total, query);
    }

    /**
     * 统计当前读者的未读站内信数（首页摘要复用）。
     *
     * @return 未读条数
     */
    public int unreadCount() {
        return Math.toIntExact(inboxRepository.unreadCount(UserContext.requireId()));
    }

    /**
     * 标记当前读者的站内信为已读。
     *
     * <p>{@code all=true} 时忽略 {@code ids}；否则必须给出非空 ID 列表，非法 ID 直接按参数错误拒绝。
     *
     * @param rawIds 站内信 ID 字符串列表
     * @param all    是否全部已读
     * @return 实际更新条数
     */
    @Transactional
    public int markRead(List<String> rawIds, boolean all) {
        long readerId = UserContext.requireId();
        if (all) {
            return inboxRepository.markAllRead(readerId);
        }
        List<Long> ids = parseIds(rawIds);
        return inboxRepository.markRead(readerId, ids);
    }

    private static List<Long> parseIds(List<String> rawIds) {
        Set<Long> ids = new LinkedHashSet<>();
        if (rawIds != null) {
            for (String raw : rawIds) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                try {
                    ids.add(Long.parseLong(raw.trim()));
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "站内信 ID 必须是数字");
                }
            }
        }
        if (ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请指定 ids 或 all=true");
        }
        if (ids.size() > MARK_READ_LIMIT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "单次最多标记 " + MARK_READ_LIMIT + " 条");
        }
        return new ArrayList<>(ids);
    }

    private static ReaderNotifyDtos.NotificationItem toItem(InboxMessage message) {
        return new ReaderNotifyDtos.NotificationItem(
                String.valueOf(message.id()),
                message.level(),
                NotifyCopy.title(message.level()),
                NotifyCopy.body(message.level(), message.orderId(), message.bookTitle(), message.overdueDays()),
                message.orderId() == null ? null : String.valueOf(message.orderId()),
                Times.iso(message.sentAt()),
                !message.unread());
    }
}
