package com.zian.backend.appeal.interfaces.dto;

import com.zian.backend.appeal.domain.model.AppealTicket;
import com.zian.backend.shared.common.Times;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 申诉入参与返回（列表读模型在 reporting）。
 */
public final class AppealDtos {

    private AppealDtos() {
    }

    public record ActionRequest(String note, Integer days) {
    }

    public record CreateRequest(
            @NotBlank(message = "申诉对象类型不能为空") String targetType,
            @NotNull(message = "申诉对象不能为空") Long targetId,
            @NotBlank(message = "申诉理由不能为空") String reason) {
    }

    public record AppealActionView(Long id, String status, Integer compensationDays) {

        public static AppealActionView of(AppealTicket ticket) {
            return new AppealActionView(ticket.getId(), ticket.getStatus().name(), ticket.getCompensationDays());
        }
    }

    /** 读者端我的申诉。 */
    public record MyTicketView(
            Long id,
            String code,
            String targetType,
            Long targetId,
            String reason,
            String status,
            String createdAt,
            String slaDueAt,
            Integer compensationDays,
            String resolution) {

        public static MyTicketView of(AppealTicket ticket) {
            return new MyTicketView(
                    ticket.getId(),
                    ticket.getTicketNo(),
                    ticket.getTargetType(),
                    ticket.getTargetId(),
                    ticket.getReason(),
                    ticket.getStatus().name(),
                    Times.iso(ticket.getCreatedAt()),
                    Times.iso(ticket.getSlaDueAt()),
                    ticket.getCompensationDays(),
                    ticket.getResolution());
        }
    }
}
