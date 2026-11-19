package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.PrivacyState;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 读者隐私授权与遗忘的仓储端口。
 */
public interface PrivacyRepository {

    /**
     * 读取授权状态；档案不存在时返回空。
     *
     * @param userId 读者账号
     * @return 授权状态
     */
    Optional<PrivacyState> find(long userId);

    /**
     * 写入（或补建）授权状态；档案行不存在时自动插入。
     *
     * @param userId 读者账号
     * @param state 最新授权状态
     */
    void save(long userId, PrivacyState state);

    /**
     * 执行脱敏：清空姓名、手机号、邮箱与院系/专业/年级，并关闭两项授权、记录遗忘时间。
     *
     * <p>借阅、预约、罚金等事实数据保持不变，仅去除可识别个人身份的信息。
     *
     * @param userId 读者账号
     * @param displayName 脱敏后的展示名
     * @param now 遗忘时间
     */
    void anonymize(long userId, String displayName, LocalDateTime now);
}
