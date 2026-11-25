package com.zian.backend.shared.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 客户端 IP 解析：仅私网/回环来源信任转发头。
 */
class WebUtilsTest {

    @Test
    void privateProxyTrustsForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.17.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.0.0.1");
        assertThat(WebUtils.getClientIp(request)).isEqualTo("203.0.113.9");
    }

    @Test
    void publicClientIgnoresForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        assertThat(WebUtils.getClientIp(request)).isEqualTo("203.0.113.10");
    }
}
