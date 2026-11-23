package com.zian.backend.appeal.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code appeal_evidence}（申诉工单与附件关联，B-09）的 MyBatis-Plus 持久化对象。
 */
@TableName("appeal_evidence")
@Data
public class AppealEvidencePO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long appealId;

    private Long fileId;

    private LocalDateTime createdAt;
}
