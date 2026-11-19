package com.zian.backend.shared.file.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code file_object}（上传文件元数据，B-08）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；磁盘内容路径保存在 {@code storage_path}。
 */
@TableName("file_object")
@Data
public class FileObjectPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long ownerId;

    private String originalName;

    private String contentType;

    private Long sizeBytes;

    private String sha256;

    private String storagePath;

    private LocalDateTime createdAt;
}
