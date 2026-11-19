package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.InboxMessage;
import java.util.List;

/**
 * 站内信读模型端口。
 *
 * <p>只读列表与「标记已读」两类操作；所有方法都以读者 ID 为边界，调用方不得传入他人 ID。
 */
public interface NotifyInboxRepository {

    /**
     * 分页查询读者的站内信，按投递时间倒序。
     *
     * @param readerId 读者 ID
     * @param offset   偏移量
     * @param limit    单页条数
     * @return 站内信列表
     */
    List<InboxMessage> page(long readerId, long offset, int limit);

    /**
     * 统计读者的站内信总数。
     *
     * @param readerId 读者 ID
     * @return 总条数
     */
    long count(long readerId);

    /**
     * 统计读者的未读站内信数。
     *
     * @param readerId 读者 ID
     * @return 未读条数
     */
    long unreadCount(long readerId);

    /**
     * 把读者的指定站内信标记为已读（已读的保持不变）。
     *
     * @param readerId 读者 ID
     * @param ids      站内信 ID 列表，不能为空
     * @return 实际更新条数
     */
    int markRead(long readerId, List<Long> ids);

    /**
     * 把读者的全部未读站内信标记为已读。
     *
     * @param readerId 读者 ID
     * @return 实际更新条数
     */
    int markAllRead(long readerId);
}
