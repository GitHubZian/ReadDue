package com.zian.backend.grace.interfaces.dto;

import com.zian.backend.grace.domain.model.GraceApplication;
import jakarta.validation.constraints.NotNull;

/**
 * 宽限审批入参与返回（列表读模型在 reporting）。
 */
public final class GraceDtos {

    private GraceDtos() {
    }

    public record ApproveRequest(Integer days, String note) {
    }

    public record RejectRequest(String note) {
    }

    public record ApplyRequest(
            @NotNull(message = "借阅单不能为空") Long borrowOrderId, String sceneCode, String evidenceText) {
    }

    /** 读者端申请条目 */
    public record ApplicationView(
            Long id,
            String code,
            Long borrowOrderId,
            String scene,
            String status,
            String requestedDays,
            Integer approvedDays,
            String createdAt,
            String snapshotExpiresAt) {
    }

    public record GraceDecisionView(Long id, String status, Integer grantedDays) {

        public static GraceDecisionView of(GraceApplication application) {
            return new GraceDecisionView(
                    application.getId(), application.getStatus().name(), application.getGrantedDays());
        }
    }
}
