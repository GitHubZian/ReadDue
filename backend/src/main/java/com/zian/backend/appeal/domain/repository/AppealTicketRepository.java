package com.zian.backend.appeal.domain.repository;

import com.zian.backend.appeal.domain.model.AppealTicket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 申诉工单仓储。
 */
public interface AppealTicketRepository {

    Optional<AppealTicket> findById(Long id);

    /** 已到期但尚未补偿的进行中工单 */
    List<AppealTicket> findSlaDue(LocalDateTime now, int limit);

    int update(AppealTicket ticket);
}
