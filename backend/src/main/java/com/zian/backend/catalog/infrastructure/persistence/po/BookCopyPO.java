package com.zian.backend.catalog.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code book_copy}（馆藏册）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("book_copy")
@Data
public class BookCopyPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long bookId;

    /** 馆藏条码（册级唯一） */
    private String barcode;

    /** 馆藏位置，如 A区3排2层 */
    private String location;

    /** 册状态 枚举：AVAILABLE/ON_LOAN/READY_SHELF/LOST/DAMAGED/REPAIRING/WITHDRAWN */
    private String status;

    private LocalDate acquiredAt;

    /** 乐观锁版本号（借出/归还并发） */
    @Version
    private Integer version;

    /** 0=未删除；删除时写入本行 id */
    private Long deleted;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    private LocalDateTime updatedAt;

}
