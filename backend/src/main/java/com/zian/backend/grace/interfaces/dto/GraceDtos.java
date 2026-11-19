package com.zian.backend.grace.interfaces.dto;

import com.zian.backend.grace.domain.model.GraceApplication;
import com.zian.backend.shared.file.domain.model.StoredFile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

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

    /**
     * 读者提交宽限申请。
     *
     * @param borrowOrderId 借阅单 id
     * @param sceneCode     场景（NORMAL/EXAM/THESIS/GRADUATION）
     * @param evidenceText  文字说明
     * @param evidenceIds   已上传的证据文件 id（先调 {@code POST /v1/files}），最多 5 个
     */
    public record ApplyRequest(
            @NotNull(message = "借阅单不能为空") Long borrowOrderId,
            String sceneCode,
            String evidenceText,
            @Size(max = 5, message = "最多上传 5 个证据附件") List<Long> evidenceIds) {
    }

    /**
     * 证据附件视图。
     *
     * @param id          文件 id（字符串）
     * @param name        原始文件名
     * @param contentType MIME
     * @param size        字节数（字符串形式）
     */
    public record EvidenceView(String id, String name, String contentType, long size) {

        /**
         * 由文件元数据构造证据视图。
         *
         * @param file 文件元数据
         * @return 视图
         */
        public static EvidenceView of(StoredFile file) {
            return new EvidenceView(
                    String.valueOf(file.id()), file.originalName(), file.contentType(), file.sizeBytes());
        }
    }

    /**
     * 读者端申请条目。
     *
     * @param id                申请 id
     * @param code              申请单号
     * @param borrowOrderId     借阅单 id
     * @param scene             场景
     * @param status            状态
     * @param requestedDays     计算天数（字符串，支持 0.5）
     * @param approvedDays      实批天数
     * @param createdAt         提交时间（+08:00）
     * @param snapshotExpiresAt 快照过期时间（+08:00）
     * @param evidence          证据附件（无则为空列表）
     */
    public record ApplicationView(
            Long id,
            String code,
            Long borrowOrderId,
            String scene,
            String status,
            String requestedDays,
            Integer approvedDays,
            String createdAt,
            String snapshotExpiresAt,
            List<EvidenceView> evidence) {
    }

    public record GraceDecisionView(Long id, String status, Integer grantedDays) {

        public static GraceDecisionView of(GraceApplication application) {
            return new GraceDecisionView(
                    application.getId(), application.getStatus().name(), application.getGrantedDays());
        }
    }
}
