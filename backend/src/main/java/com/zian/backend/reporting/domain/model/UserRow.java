package com.zian.backend.reporting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户行：账号 + 院系 + 在借册数 + 逾期天数合计（用于实时算罚金）。
 *
 * @param id              用户主键
 * @param name            姓名
 * @param userNo          学号/工号
 * @param role            角色
 * @param status          账号状态
 * @param phone           手机号
 * @param email           邮箱
 * @param createdAt       注册时间
 * @param lastLoginAt     最近登录时间；从未登录时为 null
 * @param blacklistReason 拉黑原因；正常状态时为 null
 * @param department      院系
 * @param borrowCount     历史借阅单数
 * @param overdueDays     当前在借的逾期天数合计（DECIMAL）
 */
public record UserRow(
        Long id,
        String name,
        String userNo,
        String role,
        String status,
        String phone,
        String email,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        String blacklistReason,
        String department,
        Long borrowCount,
        BigDecimal overdueDays) {
}
