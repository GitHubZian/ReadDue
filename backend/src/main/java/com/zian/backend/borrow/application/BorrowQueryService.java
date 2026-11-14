package com.zian.backend.borrow.application;

import com.zian.backend.borrow.domain.model.BorrowOrder;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 借阅只读查询（供其他上下文通过 application 接口访问，不暴露 Mapper）。
 */
@Service
@RequiredArgsConstructor
public class BorrowQueryService {

    private final BorrowOrderRepository repository;

    public record ActiveBorrow(Long id, Long readerId, Long bookId, LocalDate dueAt, LocalDate effectiveDueAt) {
    }

    /**
     * 批量读取在借单据，供通知模块补全读者与到期信息。
     *
     * @param ids 借阅单 ID 集合
     * @return 在借单据，已过滤结束态与不存在的 ID
     */
    public List<ActiveBorrow> findActive(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findByIds(ids).stream()
                .filter(BorrowOrder::isActive)
                .map(order -> new ActiveBorrow(
                        order.getId(),
                        order.getReaderId(),
                        order.getBookId(),
                        order.getDueAt(),
                        order.getEffectiveDueAt()))
                .toList();
    }
}
