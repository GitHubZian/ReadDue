package com.zian.backend.shared.file.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/**
 * B-19 上传加固：仅文件头伪造的 PNG/JPEG 必须被真实解码校验拒绝。
 */
class FileUploadValidationTest extends SecurityTestSupport {

    private static final byte[] REAL_PNG = Base64.getDecoder()
            .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    /** 12 字节 PNG 文件头 + 垃圾字节：历史漏洞可入库的伪 PNG。 */
    private static final byte[] HEADER_ONLY_PNG =
            new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4};

    @Test
    void rejectHeaderOnlyPng() throws Exception {
        String token = login(newReader());

        mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", "fake.png", "image/png", HEADER_ONLY_PNG))
                        .header("Authorization", bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.message").value("文件内容与扩展名不一致"));
    }

    @Test
    void rejectImageWhoseRealFormatDiffersFromExtension() throws Exception {
        String token = login(newReader());

        mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", "renamed.jpg", "image/jpeg", REAL_PNG))
                        .header("Authorization", bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("文件内容与扩展名不一致"));
    }

    @Test
    void acceptRealPng() throws Exception {
        String token = login(newReader());

        mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", "real.png", "image/png", REAL_PNG))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("real.png"));
    }

    private String newReader() {
        String readerNo = newAccount("up");
        createUser(readerNo, "READER", "NORMAL", null);
        return readerNo;
    }
}
