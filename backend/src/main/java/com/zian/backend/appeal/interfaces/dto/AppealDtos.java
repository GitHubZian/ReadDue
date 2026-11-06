package com.zian.backend.appeal.interfaces.dto;

import com.zian.backend.appeal.domain.model.AppealTicket;

/**
 * 申诉处理入参与返回（列表读模型在 reporting）。
 */
public final class AppealDtos {

    private AppealDtos() {
    }

    public record ActionRequest(String note, Integer days) {
    }

    public record AppealActionView(Long id, String status, Integer compensationDays) {

        public static AppealActionView of(AppealTicket ticket) {
            return new AppealActionView(ticket.getId(), ticket.getStatus().name(), ticket.getCompensationDays());
        }
    }
}
