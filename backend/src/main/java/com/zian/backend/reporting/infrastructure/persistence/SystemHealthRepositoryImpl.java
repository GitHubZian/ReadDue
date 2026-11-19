package com.zian.backend.reporting.infrastructure.persistence;

import com.zian.backend.reporting.domain.repository.SystemHealthRepository;
import com.zian.backend.reporting.infrastructure.persistence.mapper.SystemHealthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 系统自检端口实现：委托 {@link SystemHealthMapper} 查元数据。
 */
@Repository
@RequiredArgsConstructor
public class SystemHealthRepositoryImpl implements SystemHealthRepository {

    private final SystemHealthMapper systemHealthMapper;

    @Override
    public Integer countBaseTables() {
        return systemHealthMapper.countBaseTables();
    }
}
