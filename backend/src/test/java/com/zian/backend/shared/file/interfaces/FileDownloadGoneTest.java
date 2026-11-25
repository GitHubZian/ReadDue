package com.zian.backend.shared.file.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zian.backend.security.SecurityTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * task-8：证据文件磁盘内容缺失时应返回 410 Gone，提示重新上传，而不是 500 系统错误。
 */
class FileDownloadGoneTest extends SecurityTestSupport {

    @Test
    @DisplayName("文件内容缺失返回 410 / code 41000")
    void missingContentReturnsGone() throws Exception {
        String userNo = newAccount("FLE");
        long userId = createUser(userNo, "READER", "NORMAL", null);
        String token = login(userNo);

        long fileId = IdWorker.getId();
        jdbc.update(
                "INSERT INTO file_object (id, owner_id, original_name, content_type, size_bytes, sha256, storage_path, "
                        + "created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                fileId,
                userId,
                "evidence.png",
                "image/png",
                12L,
                "a".repeat(64),
                "missing/" + fileId + ".png");

        mockMvc.perform(get("/v1/files/" + fileId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value(41000))
                .andExpect(jsonPath("$.message").value("文件已失效，请重新上传"));
    }
}
