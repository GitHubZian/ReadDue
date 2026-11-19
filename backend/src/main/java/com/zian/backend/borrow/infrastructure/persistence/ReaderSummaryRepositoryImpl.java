package com.zian.backend.borrow.infrastructure.persistence;

import com.zian.backend.borrow.domain.repository.ReaderSummaryRepository;
import com.zian.backend.borrow.infrastructure.persistence.mapper.ReaderSummaryMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读者首页摘要仓储实现：直接委托 Mapper。
 */
@Repository
@RequiredArgsConstructor
public class ReaderSummaryRepositoryImpl implements ReaderSummaryRepository {

    private final ReaderSummaryMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Counts counts(long readerId, LocalDate today, LocalDate plus3Days, LocalDateTime termStart) {
        return mapper.counts(readerId, today, plus3Days, termStart);
    }
}
