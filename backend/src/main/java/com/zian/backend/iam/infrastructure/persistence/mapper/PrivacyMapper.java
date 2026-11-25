package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.zian.backend.iam.domain.model.PrivacyState;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 隐私授权与遗忘的只写/只读 SQL（跨 {@code reader_profile} 与 {@code sys_user} 两张 IAM 自有表）。
 */
@Mapper
public interface PrivacyMapper {

    /**
     * 读取读者档案中的授权状态。
     *
     * @param userId 读者账号
     * @return 授权状态
     */
    @Select("SELECT scene_consent AS sceneConsent, cache_consent AS cacheConsent, "
            + "consent_revoked_at AS consentRevokedAt, forget_requested_at AS forgetRequestedAt "
            + "FROM reader_profile WHERE user_id = #{userId}")
    Optional<PrivacyState> find(@Param("userId") long userId);

    /**
     * 写入授权状态；档案行不存在时补建。
     *
     * @param userId 读者账号
     * @param sceneConsent 场景声明授权
     * @param cacheConsent 缓存授权
     * @param revokedAt 关闭授权时间
     */
    @Insert("INSERT INTO reader_profile "
            + "(user_id, scene_consent, cache_consent, consent_revoked_at, created_at, updated_at) "
            + "VALUES (#{userId}, #{sceneConsent}, #{cacheConsent}, #{revokedAt}, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE scene_consent = #{sceneConsent}, "
            + "cache_consent = #{cacheConsent}, consent_revoked_at = #{revokedAt}, updated_at = NOW()")
    void save(
            @Param("userId") long userId,
            @Param("sceneConsent") boolean sceneConsent,
            @Param("cacheConsent") boolean cacheConsent,
            @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * 脱敏账号：姓名改为展示名，手机号与邮箱清空。
     *
     * @param userId 读者账号
     * @param displayName 脱敏后的展示名
     */
    @Update("UPDATE sys_user SET name = #{displayName}, phone = NULL, email = NULL, status = 'DISABLED', "
            + "updated_at = NOW() WHERE id = #{userId} AND deleted = 0")
    void anonymizeUser(@Param("userId") long userId, @Param("displayName") String displayName);

    /**
     * 脱敏读者档案：院系/专业/年级清空，授权关闭并记录遗忘时间。
     *
     * @param userId 读者账号
     * @param now 遗忘时间
     */
    @Update("UPDATE reader_profile SET department = NULL, major = NULL, grade = NULL, "
            + "scene_consent = 0, cache_consent = 0, consent_revoked_at = #{now}, "
            + "forget_requested_at = #{now}, updated_at = NOW() WHERE user_id = #{userId}")
    void anonymizeProfile(@Param("userId") long userId, @Param("now") LocalDateTime now);
}
