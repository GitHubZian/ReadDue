package com.zian.backend.shared.common;

import com.zian.backend.shared.exception.BusinessException;

/**
 * 统一分页查询参数。
 *
 * <p>约定：page 从 1 开始；size 默认 20，上限 200，超过上限直接按参数错误拒绝（不做静默截断，
 * 避免调用方误以为拿全了数据）。
 */
public record PageQuery(int page, int size, String sort) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public static PageQuery of(Integer page, Integer size, String sort) {
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
        return new PageQuery(p, s, sort);
    }

    public long offset() {
        return (long) (page - 1) * size;
    }
}
