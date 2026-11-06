package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code sys_role}（角色（三级：管理员 / 馆员 / 用户））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("sys_role")
@Data
public class SysRolePO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** ADMIN / LIBRARIAN / READER */
    private String roleCode;

    /** 管理员 / 馆员 / 用户 */
    private String roleName;

    /** 数据范围 枚举：ALL/LIBRARY/SELF */
    private String dataScope;

    private String description;

    private Integer enabled;

    /** 0=未删除；删除时写入本行 id */
    private Long deleted;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    private LocalDateTime updatedAt;

}
