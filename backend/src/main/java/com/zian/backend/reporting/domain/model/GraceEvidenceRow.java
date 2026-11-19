package com.zian.backend.reporting.domain.model;

/**
 * 宽限单证据附件行（管理端展示）：只读联表结果，文件本体由 {@code shared/file} 管理。
 *
 * @param graceId 宽限申请 id
 * @param fileId  文件 id
 * @param name    原始文件名
 */
public record GraceEvidenceRow(Long graceId, Long fileId, String name) {
}
