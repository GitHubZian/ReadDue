package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code reader_profile}（读者档案与隐私授权状态）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("reader_profile")
@Data
public class ReaderProfilePO {

    /** 读者账号 */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 院系 */
    private String department;

    /** 专业 */
    private String major;

    /** 年级 */
    private String grade;

    /** 场景声明授权：0 未授权（默认最小授权）/ 1 已授权 */
    private Integer sceneConsent;

    /** 宽限计算缓存授权（TTL 24h）：0/1 */
    private Integer cacheConsent;

    /** 最近一次关闭授权时间，触发立即遗忘 */
    private LocalDateTime consentRevokedAt;

    /** 读者点「立即遗忘」的时间 */
    private LocalDateTime forgetRequestedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
