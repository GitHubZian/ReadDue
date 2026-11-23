package com.zian.backend.reporting.domain.model;

/**
 * 申诉工单证据附件行（管理端展示）：只读联表结果，文件本体由 {@code shared/file} 管理。
 *
 * @param appealId 申诉工单 id
 * @param fileId   文件 id
 * @param name     原始文件名
 */
public record AppealEvidenceRow(Long appealId, Long fileId, String name) {
}
