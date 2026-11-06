package com.zian.backend.bootstrap.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 校验雪花 id 的 JSON 序列化契约：Long / long 必须输出字符串。
 */
@SpringBootTest
class JsonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void longIsSerializedAsString() throws Exception {
        assertThat(objectMapper.writeValueAsString(1234567890123456789L))
                .isEqualTo("\"1234567890123456789\"");

        assertThat(objectMapper.writeValueAsString(Map.of("id", 42L)))
                .isEqualTo("{\"id\":\"42\"}");
    }
}
