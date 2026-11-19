package com.zian.backend.notify.infrastructure.persistence.mapper;

import com.zian.backend.notify.domain.model.InboxMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 站内信读模型 SQL。
 *
 * <p>站内信 = {@code notify_delivery} 中 {@code channel='INBOX' AND status='SENT'} 的记录；
 * 读者归属通过 {@code notify_task.reader_id} 判定，SQL 内强制带该条件。
 */
@Mapper
public interface NotifyInboxMapper {

    /**
     * 分页查询读者的站内信。
     *
     * @param readerId 读者 ID
     * @param limit    单页条数
     * @param offset   偏移量
     * @return 站内信列表
     */
    @Select(
            """
            SELECT d.id,
                   t.level,
                   t.borrow_order_id                                            AS orderId,
                   b.title                                                      AS bookTitle,
                   CAST(JSON_UNQUOTE(JSON_EXTRACT(t.payload, '$.overdueDays')) AS SIGNED) AS overdueDays,
                   COALESCE(d.sent_at, t.created_at)                            AS sentAt,
                   d.read_at                                                    AS readAt
              FROM notify_delivery d
              JOIN notify_task t ON t.id = d.task_id
              LEFT JOIN borrow_order o ON o.id = t.borrow_order_id
              LEFT JOIN book b ON b.id = o.book_id AND b.deleted = 0
             WHERE t.reader_id = #{readerId}
               AND d.channel = 'INBOX'
               AND d.status = 'SENT'
             ORDER BY d.id DESC
             LIMIT #{limit} OFFSET #{offset}
            """)
    List<InboxMessage> page(@Param("readerId") long readerId, @Param("limit") int limit, @Param("offset") long offset);

    /**
     * 统计读者的站内信总数。
     *
     * @param readerId 读者 ID
     * @return 总条数
     */
    @Select(
            """
            SELECT COUNT(*)
              FROM notify_delivery d
              JOIN notify_task t ON t.id = d.task_id
             WHERE t.reader_id = #{readerId}
               AND d.channel = 'INBOX'
               AND d.status = 'SENT'
            """)
    long count(@Param("readerId") long readerId);

    /**
     * 统计读者的未读站内信数。
     *
     * @param readerId 读者 ID
     * @return 未读条数
     */
    @Select(
            """
            SELECT COUNT(*)
              FROM notify_delivery d
              JOIN notify_task t ON t.id = d.task_id
             WHERE t.reader_id = #{readerId}
               AND d.channel = 'INBOX'
               AND d.status = 'SENT'
               AND d.read_at IS NULL
            """)
    long unreadCount(@Param("readerId") long readerId);

    /**
     * 标记指定站内信为已读。
     *
     * @param readerId 读者 ID
     * @param ids      站内信 ID 列表
     * @return 实际更新条数
     */
    @Update(
            """
            <script>
            UPDATE notify_delivery d
              JOIN notify_task t ON t.id = d.task_id
               SET d.read_at = NOW()
             WHERE t.reader_id = #{readerId}
               AND d.channel = 'INBOX'
               AND d.read_at IS NULL
               AND d.id IN
               <foreach item="id" collection="ids" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    int markRead(@Param("readerId") long readerId, @Param("ids") List<Long> ids);

    /**
     * 标记全部未读站内信为已读。
     *
     * @param readerId 读者 ID
     * @return 实际更新条数
     */
    @Update(
            """
            UPDATE notify_delivery d
              JOIN notify_task t ON t.id = d.task_id
               SET d.read_at = NOW()
             WHERE t.reader_id = #{readerId}
               AND d.channel = 'INBOX'
               AND d.read_at IS NULL
            """)
    int markAllRead(@Param("readerId") long readerId);
}
