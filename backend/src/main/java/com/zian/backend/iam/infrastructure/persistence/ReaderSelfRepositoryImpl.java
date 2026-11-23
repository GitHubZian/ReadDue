package com.zian.backend.iam.infrastructure.persistence;

import com.zian.backend.iam.domain.repository.ReaderSelfRepository;
import com.zian.backend.iam.infrastructure.persistence.mapper.ReaderProfileMapper;
import com.zian.backend.iam.infrastructure.persistence.po.ReaderProfilePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读者「我的」资料只读仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class ReaderSelfRepositoryImpl implements ReaderSelfRepository {

    private final ReaderProfileMapper readerProfileMapper;

    @Override
    public Profile profile(long userId) {
        ReaderProfilePO po = readerProfileMapper.selectById(userId);
        if (po == null) {
            return Profile.empty();
        }
        return new Profile(blankIfNull(po.getDepartment()), blankIfNull(po.getMajor()), blankIfNull(po.getGrade()));
    }

    @Override
    public long totalBorrowed(long userId) {
        return readerProfileMapper.countBorrowed(userId);
    }

    private static String blankIfNull(String value) {
        return value == null ? "" : value;
    }
}
