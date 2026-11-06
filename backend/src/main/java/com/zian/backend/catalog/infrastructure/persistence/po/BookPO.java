package com.zian.backend.catalog.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 表 {@code book}（书目）的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName(value = "book", autoResultMap = true)
@Data
public class BookPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** ISBN-13 */
    private String isbn13;

    /** 书名 */
    private String title;

    private String subtitle;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    /** 中图法分类号 */
    private String categoryCode;

    /** 语种 */
    private String language;

    private BigDecimal price;

    /** 简介 */
    private String summary;

    private String coverUrl;

    /** 标签数组，如 ["计算机","考试周高需"] */
    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 1 上架 / 0 下架 */
    private Integer status;

    /** 0=未删除；删除时写入本行 id */
    private Long deleted;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
