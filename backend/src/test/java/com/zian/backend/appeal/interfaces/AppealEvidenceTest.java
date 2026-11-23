package com.zian.backend.appeal.interfaces;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/**
 * B-09 申诉证据附件：创建携带、详情/列表回显、管理端可见与归属/数量校验。
 */
class AppealEvidenceTest extends SecurityTestSupport {

    private static final byte[] PNG = java.util.Base64.getDecoder()
            .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    @Test
    void createWithEvidenceAndReadBack() throws Exception {
        String readerNo = newAccount("ap");
        createUser(readerNo, "READER", "NORMAL", null);
        String token = login(readerNo);
        long fileId = upload(token, "appeal.png", "image/png", PNG);

        String created = create(token, List.of(String.valueOf(fileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.evidence", hasSize(1)))
                .andExpect(jsonPath("$.data.evidence[0].id").value(String.valueOf(fileId)))
                .andExpect(jsonPath("$.data.evidence[0].name").value("appeal.png"))
                .andExpect(jsonPath("$.data.evidence[0].contentType").value("image/png"))
                .andExpect(jsonPath("$.data.evidence[0].size").value(String.valueOf(PNG.length)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode data = objectMapper.readTree(created).path("data");
        long appealId = data.path("id").asLong();
        String code = data.path("code").asText();

        mockMvc.perform(get("/v1/appeals/{id}", appealId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evidence[0].name").value("appeal.png"));

        mockMvc.perform(get("/v1/appeals/self").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].evidence[0].id").value(String.valueOf(fileId)));

        String staffNo = newAccount("lib");
        createUser(staffNo, "LIBRARIAN", "NORMAL", null);
        String staffToken = login(staffNo);

        mockMvc.perform(get("/v1/admin/appeals")
                        .param("keyword", code)
                        .header("Authorization", bearer(staffToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].evidence[0].label").value("appeal.png"))
                .andExpect(jsonPath("$.data.items[0].evidence[0].url").value("/v1/files/" + fileId));

        mockMvc.perform(get("/v1/appeals/{id}", appealId).header("Authorization", bearer(staffToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evidence[0].name").value("appeal.png"));
    }

    @Test
    void legacyRequestWithoutEvidenceStillWorks() throws Exception {
        String readerNo = newAccount("ap");
        createUser(readerNo, "READER", "NORMAL", null);
        String token = login(readerNo);

        mockMvc.perform(post("/v1/appeals")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "targetType", "OTHER",
                                "targetId", 1,
                                "reason", "旧请求不带附件"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.evidence", hasSize(0)));
    }

    @Test
    void rejectsSomeoneElsesFileAndTooManyFiles() throws Exception {
        String ownerNo = newAccount("ap");
        createUser(ownerNo, "READER", "NORMAL", null);
        String otherNo = newAccount("ap");
        createUser(otherNo, "READER", "NORMAL", null);
        String ownerToken = login(ownerNo);
        String otherToken = login(otherNo);
        long fileId = upload(ownerToken, "appeal.png", "image/png", PNG);

        create(otherToken, List.of(String.valueOf(fileId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("证据附件不存在"));

        create(ownerToken, List.of("1", "2", "3", "4", "5", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.message").value(containsString("最多上传 5 个证据附件")));
    }

    @Test
    void strangerCannotReadSomeoneElsesAppeal() throws Exception {
        String ownerNo = newAccount("ap");
        createUser(ownerNo, "READER", "NORMAL", null);
        String otherNo = newAccount("ap");
        createUser(otherNo, "READER", "NORMAL", null);
        String ownerToken = login(ownerNo);

        String created = create(ownerToken, List.of())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        long appealId = objectMapper.readTree(created).path("data").path("id").asLong();

        mockMvc.perform(get("/v1/appeals/{id}", appealId).header("Authorization", bearer(login(otherNo))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("申诉工单不存在"));
    }

    private org.springframework.test.web.servlet.ResultActions create(String token, List<String> evidenceIds)
            throws Exception {
        return mockMvc.perform(post("/v1/appeals")
                .header("Authorization", bearer(token))
                .contentType(JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "targetType", "OTHER",
                        "targetId", 1,
                        "reason", "测试申诉证据",
                        "evidenceIds", evidenceIds))));
    }

    private long upload(String token, String name, String contentType, byte[] content) throws Exception {
        String body = mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", name, contentType, content))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
