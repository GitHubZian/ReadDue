package com.zian.backend.shared.file.domain.model;

import java.time.LocalDateTime;

/**
 * 已存储文件的元数据（磁盘只保存相对路径，鉴权在应用层完成）。
 *
 * @param id           文件 id（雪花）
 * @param ownerId      上传者 id
 * @param originalName 原始文件名（已去路径与控制字符）
 * @param contentType  规范后的 MIME，仅 image/png、image/jpeg、application/pdf
 * @param sizeBytes    字节数
 * @param sha256       内容摘要（十六进制，用于去重与取证）
 * @param storagePath  相对存储根目录的路径
 * @param createdAt    上传时间
 */
public record StoredFile(
        Long id,
        Long ownerId,
        String originalName,
        String contentType,
        long sizeBytes,
        String sha256,
        String storagePath,
        LocalDateTime createdAt) {
}
