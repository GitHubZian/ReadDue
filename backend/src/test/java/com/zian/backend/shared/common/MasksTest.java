package com.zian.backend.shared.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 脱敏工具：邮箱只露首字符与域名，自由文本去控制字符并截断。
 */
class MasksTest {

    @Test
    void keepsFirstCharacterAndDomain() {
        assertThat(Masks.email("reader@example.com")).isEqualTo("r***@example.com");
        assertThat(Masks.email("a@b.com")).isEqualTo("a***@b.com");
    }

    @Test
    void hidesNullOrMalformedAddress() {
        assertThat(Masks.email(null)).isEqualTo("***");
        assertThat(Masks.email("")).isEqualTo("***");
        assertThat(Masks.email("   ")).isEqualTo("***");
        assertThat(Masks.email("no-at-sign")).isEqualTo("***");
        assertThat(Masks.email("@qq.com")).isEqualTo("***");
    }

    @Test
    void stripsLineBreaksThatCouldForgeLogLines() {
        assertThat(Masks.email("a\r\nb@qq.com")).isEqualTo("a***@qq.com");
        assertThat(Masks.text("boom\r\nfake log line", 200)).isEqualTo("boom  fake log line");
    }

    @Test
    void keepsMultipleAtSignsBehindMask() {
        assertThat(Masks.email("a@b@c.com")).isEqualTo("a***@b@c.com");
    }

    @Test
    void truncatesLongTextForColumnWidth() {
        String longText = "x".repeat(500);
        String result = Masks.text(longText, 200);
        assertThat(result).hasSize(200);
        assertThat(result).endsWith("…");
        assertThat(Masks.text("y".repeat(200), 200)).hasSize(200).doesNotEndWith("…");
        assertThat(Masks.text(null, 200)).isEmpty();
    }
}
