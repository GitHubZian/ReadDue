package com.zian.backend.iam.infrastructure.persistence;

import com.zian.backend.iam.domain.model.PrivacyState;
import com.zian.backend.iam.domain.repository.PrivacyRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.PrivacyMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 隐私授权与遗忘的仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class PrivacyRepositoryImpl implements PrivacyRepository {

    private final PrivacyMapper mapper;

    @Override
    public Optional<PrivacyState> find(long userId) {
        return mapper.find(userId);
    }

    @Override
    public void save(long userId, PrivacyState state) {
        mapper.save(userId, state.sceneConsent(), state.cacheConsent(), state.consentRevokedAt());
    }

    @Override
    public void anonymize(long userId, String displayName, LocalDateTime now) {
        mapper.anonymizeUser(userId, displayName);
        mapper.anonymizeProfile(userId, now);
    }
}
