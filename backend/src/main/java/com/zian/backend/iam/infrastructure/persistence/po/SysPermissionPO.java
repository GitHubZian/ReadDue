package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code sys_permission}（权限码（菜单 / 按钮 / 接口 / 数据范围））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("sys_permission")
@Data
public class SysPermissionPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 如 borrow:return / user:blacklist */
    private String permCode;

    private String permName;

    /** 枚举：MENU/BUTTON/API/DATA */
    private String permType;

    /** 0 为根节点 */
    private Long parentId;

    /** 前端路由（MENU）或接口路径（API） */
    private String path;

    /** HTTP 方法（API） */
    private String method;

    private Integer sort;

    private Integer enabled;

    private Long deleted;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    private LocalDateTime updatedAt;

}
