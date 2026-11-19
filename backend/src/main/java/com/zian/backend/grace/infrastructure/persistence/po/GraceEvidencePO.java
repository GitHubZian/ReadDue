package com.zian.backend.grace.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code grace_evidence}（宽限申请与附件关联，B-08）的 MyBatis-Plus 持久化对象。
 */
@TableName("grace_evidence")
@Data
public class GraceEvidencePO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long graceId;

    private Long fileId;

    private LocalDateTime createdAt;
}
