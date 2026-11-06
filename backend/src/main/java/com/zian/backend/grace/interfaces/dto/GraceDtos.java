package com.zian.backend.grace.interfaces.dto;

import com.zian.backend.grace.domain.model.GraceApplication;

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

    public record GraceDecisionView(Long id, String status, Integer grantedDays) {

        public static GraceDecisionView of(GraceApplication application) {
            return new GraceDecisionView(
                    application.getId(), application.getStatus().name(), application.getGrantedDays());
        }
    }
}
