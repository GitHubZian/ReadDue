package com.zian.backend.appeal.interfaces.dto;

import com.zian.backend.appeal.domain.model.AppealTicket;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.file.domain.model.StoredFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

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
            @NotBlank(message = "申诉理由不能为空") String reason,
            @Size(max = 5, message = "最多上传 5 个证据附件") List<Long> evidenceIds) {
    }

    public record AppealActionView(Long id, String status, Integer compensationDays) {

        public static AppealActionView of(AppealTicket ticket) {
            return new AppealActionView(ticket.getId(), ticket.getStatus().name(), ticket.getCompensationDays());
        }
    }

    /** 申诉证据附件（读者端详情）。 */
    public record EvidenceView(String id, String name, String contentType, long size) {

        public static EvidenceView of(StoredFile file) {
            return new EvidenceView(
                    String.valueOf(file.id()),
                    file.originalName(),
                    file.contentType(),
                    file.sizeBytes());
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
            String resolution,
            List<EvidenceView> evidence) {

        public static MyTicketView of(AppealTicket ticket, List<StoredFile> evidence) {
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
                    ticket.getResolution(),
                    evidence.stream().map(EvidenceView::of).toList());
        }
    }
}
