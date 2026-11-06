package com.zian.backend.catalog.domain.model;

/**
 * 书目（贫血模型：只承载展示与检索需要的字段）。
 */
public record Book(Long id, String title, String author, String coverUrl) {
}
