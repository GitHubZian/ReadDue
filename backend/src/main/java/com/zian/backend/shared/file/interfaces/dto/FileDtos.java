package com.zian.backend.shared.file.interfaces.dto;

import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.file.domain.model.StoredFile;

/**
 * 文件上传/下载的入参与返回。
 *
 * <p>{@code size} 与 {@code id} 为长整型，按全局 Jackson 配置以字符串输出，避免前端精度丢失。
 */
public final class FileDtos {

    private FileDtos() {
    }

    /**
     * 文件元数据视图。
     *
     * @param id          文件 id（字符串形式的雪花 id）
     * @param name        原始文件名
     * @param contentType MIME
     * @param size        字节数（字符串形式）
     * @param createdAt   上传时间（+08:00）
     */
    public record FileView(String id, String name, String contentType, long size, String createdAt) {

        /**
         * 由领域元数据构造视图。
         *
         * @param file 元数据
         * @return 视图
         */
        public static FileView of(StoredFile file) {
            return new FileView(
                    String.valueOf(file.id()),
                    file.originalName(),
                    file.contentType(),
                    file.sizeBytes(),
                    Times.iso(file.createdAt()));
        }
    }

    /**
     * 下载结果（文件名、MIME 与内容）。
     *
     * @param name        原始文件名
     * @param contentType MIME
     * @param content     字节内容
     */
    public record Download(String name, String contentType, byte[] content) {
    }
}
