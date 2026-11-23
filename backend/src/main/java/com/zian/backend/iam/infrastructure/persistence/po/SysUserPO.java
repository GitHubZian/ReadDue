package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code sys_user}（账号（读者/馆员/管理员））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("sys_user")
@Data
public class SysUserPO {

    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 账号：读者学号 / 馆员工号 */
    private String userNo;

    /** 主角色（单角色，多角色需另建关联表） 枚举：READER/LIBRARIAN/ADMIN */
    private String role;

    /** 姓名 */
    private String name;

    /** 手机号（仅用于通知渠道，不参与画像） */
    private String phone;

    /** 邮箱（L2/L3 提醒渠道） */
    private String email;

    /** BCrypt */
    private String passwordHash;

    /** 账号状态：NORMAL 正常 / BLACKLISTED 拉黑 枚举：NORMAL/BLACKLISTED */
    private String status;

    /** 拉黑原因（读者可见） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String blacklistReason;

    /** 拉黑时间 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime blacklistedAt;

    /** 拉黑操作人（仅管理员） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long blacklistedBy;

    /** 到期自动解除；NULL 表示需人工恢复 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime blacklistExpiresAt;

    private LocalDateTime lastLoginAt;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 0=未删除；删除时写入本行 id，保证唯一键可复用 */
    private Long deleted;

    /** 逻辑删除时间 */
    private LocalDateTime deletedAt;

    /** 删除操作人 */
    private Long deletedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
