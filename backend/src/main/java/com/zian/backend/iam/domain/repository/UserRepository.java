package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.User;
import java.util.Optional;

/**
 * 账号仓储。
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUserNo(String userNo);

    /** 乐观锁更新；返回受影响行数，0 表示并发冲突 */
    int update(User user);

}
