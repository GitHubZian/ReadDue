package com.zian.backend.iam.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code deletion_request}（读者注销申请，M6）的 MyBatis-Plus 持久化对象。
 */
@TableName("deletion_request")
@Data
public class DeletionRequestPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String status;

    private String reason;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime handledAt;

    private Long handledBy;
}
