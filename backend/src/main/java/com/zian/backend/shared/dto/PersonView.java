package com.zian.backend.shared.dto;

/**
 * 读者视图（对应前端 Person）。
 */
public record PersonView(Long id, String name, String studentNo, String college) {
}
