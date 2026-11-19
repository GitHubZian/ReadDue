package com.zian.backend.reporting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宽限单行：宽限申请快照 + 读者 + 关联借阅的书目与副本。
 *
 * @param id           宽限单主键
 * @param applyNo      申请单号
 * @param sceneCode    读者自声明场景
 * @param evidenceText 一句话依据说明
 * @param status       审批状态
 * @param computedDays 公式计算天数（DECIMAL(4,1)）
 * @param grantedDays  实际批准天数；未审批时为 null
 * @param createdAt    提交时间
 * @param decidedAt    审批时间；未审批时为 null
 * @param decidedBy    审批人主键；未审批时为 null
 * @param rejectCode   驳回码；未驳回时为 null
 * @param readerId     读者主键
 * @param readerName   读者姓名
 * @param userNo       学号/工号
 * @param department   院系
 * @param bookId       书目主键
 * @param title        书名
 * @param author       作者
 * @param barcode      副本条码
 */
public record GraceRow(
        Long id,
        String applyNo,
        String sceneCode,
        String evidenceText,
        String status,
        BigDecimal computedDays,
        Integer grantedDays,
        LocalDateTime createdAt,
        LocalDateTime decidedAt,
        Long decidedBy,
        String rejectCode,
        Long readerId,
        String readerName,
        String userNo,
        String department,
        Long bookId,
        String title,
        String author,
        String barcode) {
}
