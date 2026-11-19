package com.zian.backend.shared.common;

import com.zian.backend.shared.exception.BusinessException;

/**
 * 统一分页查询参数。
 *
 * <p>约定：page 从 1 开始；size 默认 20，上限 200，超过上限直接按参数错误拒绝（不做静默截断，
 * 避免调用方误以为拿全了数据）。
 *
 * <p>不支持调用方自定义排序：各列表的排序由 SQL 固定（时间倒序为主），避免把客户端字符串拼进
 * {@code ORDER BY} 形成注入面。旧前端若仍传 {@code sort}，Spring 会忽略未知查询参数，不会报错。
 */
public record PageQuery(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    /**
     * 构造分页参数：页码从 1 开始，单页上限 200，越界抛参数错误。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页参数
     */
    public static PageQuery of(Integer page, Integer size) {
        int p = page == null ? 1 : page;
        int s = size == null ? DEFAULT_SIZE : size;
        if (p < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "page 必须 ≥ 1");
        }
        if (s < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "size 必须 ≥ 1");
        }
        if (s > MAX_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "size 不能超过 " + MAX_SIZE);
        }
        return new PageQuery(p, s);
    }

    /**
     * 计算 SQL 偏移量。
     *
     * @return (page - 1) * size
     */
    public long offset() {
        return (long) (page - 1) * size;
    }
}
