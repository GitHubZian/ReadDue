package com.zian.backend.iam.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.model.UserRole;
import com.zian.backend.iam.domain.model.UserStatus;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.SysUserMapper;
import com.zian.backend.iam.infrastructure.persistence.po.SysUserPO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 账号仓储实现（MyBatis-Plus）。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper mapper;

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUserNo(String userNo) {
        SysUserPO po = mapper.selectOne(Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getUserNo, userNo)
                .eq(SysUserPO::getDeleted, 0L)
                .last("limit 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public int update(User user) {
        SysUserPO po = new SysUserPO();
        po.setId(user.getId());
        po.setStatus(user.getStatus().name());
        po.setBlacklistReason(user.getBlacklistReason());
        po.setBlacklistedAt(user.getBlacklistedAt());
        po.setBlacklistedBy(user.getBlacklistedBy());
        po.setBlacklistExpiresAt(user.getBlacklistExpiresAt());
        po.setLastLoginAt(user.getLastLoginAt());
        po.setVersion(user.getVersion());
        return mapper.updateById(po);
    }

    @Override
    public List<User> findExpiredBlacklist(LocalDateTime now, int limit) {
        return mapper.selectList(Wrappers.<SysUserPO>lambdaQuery()
                        .eq(SysUserPO::getStatus, UserStatus.BLACKLISTED.name())
                        .isNotNull(SysUserPO::getBlacklistExpiresAt)
                        .le(SysUserPO::getBlacklistExpiresAt, now)
                        .last("limit " + Math.max(1, limit)))
                .stream()
                .map(this::toDomain)
                .toList();
    }


    private User toDomain(SysUserPO po) {
        return new User(
                po.getId(),
                po.getUserNo(),
                po.getName(),
                UserRole.valueOf(po.getRole()),
                po.getPhone(),
                po.getEmail(),
                po.getPasswordHash(),
                UserStatus.valueOf(po.getStatus()),
                po.getBlacklistReason(),
                po.getBlacklistedAt(),
                po.getBlacklistedBy(),
                po.getBlacklistExpiresAt(),
                po.getLastLoginAt(),
                po.getVersion(),
                po.getCreatedAt());
    }
}
