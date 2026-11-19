package com.zian.backend.notify.infrastructure.persistence;

import com.zian.backend.notify.domain.model.InboxMessage;
import com.zian.backend.notify.domain.repository.NotifyInboxRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyInboxMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 站内信读模型仓储实现：纯委托 {@link NotifyInboxMapper}。
 */
@Repository
@RequiredArgsConstructor
public class NotifyInboxRepositoryImpl implements NotifyInboxRepository {

    private final NotifyInboxMapper mapper;

    @Override
    public List<InboxMessage> page(long readerId, long offset, int limit) {
        return mapper.page(readerId, limit, offset);
    }

    @Override
    public long count(long readerId) {
        return mapper.count(readerId);
    }

    @Override
    public long unreadCount(long readerId) {
        return mapper.unreadCount(readerId);
    }

    @Override
    public int markRead(long readerId, List<Long> ids) {
        return mapper.markRead(readerId, ids);
    }

    @Override
    public int markAllRead(long readerId) {
        return mapper.markAllRead(readerId);
    }
}
