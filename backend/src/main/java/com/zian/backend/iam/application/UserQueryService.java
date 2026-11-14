package com.zian.backend.iam.application;

import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号只读查询：供其它上下文（借阅、通知、审计）按 id / 学号取读者信息。
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 按 ID 取用户，不存在时抛 404。
     *
     * @param id 用户 ID
     * @return 用户聚合
     */
    @Transactional(readOnly = true)
    public User requireById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "读者不存在"));
    }

    /**
     * 按学号/工号取用户，不存在时抛 404。
     *
     * @param userNo 学号或工号
     * @return 用户聚合
     */
    @Transactional(readOnly = true)
    public User requireByUserNo(String userNo) {
        return userRepository
                .findByUserNo(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到学号/工号为 " + userNo + " 的账号"));
    }
}
