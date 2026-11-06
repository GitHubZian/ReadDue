package com.zian.backend.grace.domain.repository;

import com.zian.backend.grace.domain.model.GraceApplication;
import java.util.Optional;

/**
 * 宽限申请仓储。
 */
public interface GraceApplicationRepository {

    Optional<GraceApplication> findById(Long id);

    int update(GraceApplication application);
}
