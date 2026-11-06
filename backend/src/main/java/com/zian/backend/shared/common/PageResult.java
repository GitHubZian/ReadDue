package com.zian.backend.shared.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;

/**
 * 统一分页返回。
 *
 * <p>字段名与前端 {@code PageResult{items,total,page,size}} 严格一致。
 *
 * <p>{@code total} 强制输出为 JSON 数字：全局配置把 Long 序列化成字符串以防 JS 精度丢失，
 * 但前端分页组件要拿 total 做算术，必须是 number。
 */
public record PageResult<T>(
        List<T> items,
        @JsonSerialize(using = RawLongSerializer.class) long total,
        int page,
        int size) {

    public static <T> PageResult<T> of(List<T> items, long total, PageQuery query) {
        return new PageResult<>(items, total, query.page(), query.size());
    }

    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(List.of(), 0L, query.page(), query.size());
    }
}
