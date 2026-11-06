package com.zian.backend.shared.dto;

/**
 * 书目视图（对应前端 BookRef）。
 */
public record BookRefView(Long id, String title, String author, String barcode, String coverTone) {
}
