package com.zian.backend.shared.outbox.application;

import com.zian.backend.shared.outbox.domain.model.OutboxEvent;
import com.zian.backend.shared.outbox.domain.repository.OutboxRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 事件发布入口（供各上下文的应用层在业务事务内调用）。
 *
 * <p>只落本地消息表，不做任何外部 IO，因此可以和业务写操作共处同一事务。
 */
@Service
@RequiredArgsConstructor
public class OutboxAppender {

    private final OutboxRepository outboxRepository;

    public void append(String aggregateType, Long aggregateId, String eventType, Map<String, Object> payload) {
        outboxRepository.append(OutboxEvent.pending(aggregateType, aggregateId, eventType, payload));
    }
}
