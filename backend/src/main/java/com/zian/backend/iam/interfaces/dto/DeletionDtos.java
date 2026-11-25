package com.zian.backend.iam.interfaces.dto;

import jakarta.validation.constraints.Size;

/**
 * 注销申请接口 DTO（M6）。
 */
public final class DeletionDtos {

    private DeletionDtos() {
    }

    /**
     * 提交注销申请。
     *
     * @param reason 注销原因，可空
     */
    public record CreateRequest(@Size(max = 255, message = "注销原因最多 255 字") String reason) {
    }

    /**
     * 驳回注销申请。
     *
     * @param note 处理备注，可空
     */
    public record RejectRequest(@Size(max = 255, message = "备注最多 255 字") String note) {
    }

    /**
     * 注销申请视图。
     */
    public record View(
            String id,
            String userId,
            String userName,
            String studentNo,
            String status,
            String reason,
            String note,
            String createdAt,
            String handledAt,
            String handledBy) {
    }
}
