package com.zian.backend.grace.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
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
 * B-08 宽限证据附件：上传校验、归属校验、申请携带与详情回显。
 */
class GraceEvidenceTest extends SecurityTestSupport {

    private static final byte[] PNG = new byte[] {
        (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4
    };

    @Test
    void uploadThenAttachToGraceAndReadBack() throws Exception {
        String readerNo = newAccount("ev");
        long readerId = createUser(readerNo, "READER", "NORMAL", null);
        Fixture book = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(readerId, book);
        String token = login(readerNo);

        long fileId = upload(token, "evidence.png", "image/png", PNG);

        String applyBody = objectMapper.writeValueAsString(Map.of(
                "borrowOrderId", orderId,
                "sceneCode", "EXAM",
                "evidenceText", "考试周复习中",
                "evidenceIds", List.of(String.valueOf(fileId))));
        String applied = mockMvc.perform(post("/v1/graces")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.evidence[0].id").value(String.valueOf(fileId)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        long graceId = objectMapper.readTree(applied).path("data").path("id").asLong();

        mockMvc.perform(get("/v1/graces/{id}", graceId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evidence[0].name").value("evidence.png"))
                .andExpect(jsonPath("$.data.evidence[0].contentType").value("image/png"));

        byte[] downloaded = mockMvc.perform(get("/v1/files/{id}", fileId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(downloaded).isEqualTo(PNG);
    }

    @Test
    void rejectOversizeAndUnsupportedFiles() throws Exception {
        String readerNo = newAccount("ev");
        createUser(readerNo, "READER", "NORMAL", null);
        String token = login(readerNo);

        byte[] tooLarge = new byte[(int) (5 * 1024 * 1024) + 1];
        tooLarge[0] = (byte) 0x89;
        tooLarge[1] = 'P';
        tooLarge[2] = 'N';
        tooLarge[3] = 'G';
        mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", "big.png", "image/png", tooLarge))
                        .header("Authorization", bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.message").value("文件不能超过 5MB"));

        mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("仅支持 png/jpg/pdf 文件"));

        mockMvc.perform(multipart("/v1/files")
                        .file(new MockMultipartFile("file", "fake.png", "image/png", "not-png".getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("文件内容与扩展名不一致"));
    }

    @Test
    void otherReaderCannotAttachOrReadSomeoneElsesFile() throws Exception {
        String ownerNo = newAccount("ev");
        long ownerId = createUser(ownerNo, "READER", "NORMAL", null);
        String otherNo = newAccount("ev");
        long otherId = createUser(otherNo, "READER", "NORMAL", null);
        Fixture book = createBookWithCopy("ON_LOAN");
        long orderId = createActiveLoan(otherId, book);

        String ownerToken = login(ownerNo);
        String otherToken = login(otherNo);
        long fileId = upload(ownerToken, "evidence.png", "image/png", PNG);

        String applyBody = objectMapper.writeValueAsString(Map.of(
                "borrowOrderId", orderId,
                "sceneCode", "EXAM",
                "evidenceIds", List.of(String.valueOf(fileId))));
        mockMvc.perform(post("/v1/graces")
                        .header("Authorization", bearer(otherToken))
                        .contentType(JSON)
                        .content(applyBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("证据附件不存在"));

        mockMvc.perform(get("/v1/files/{id}", fileId).header("Authorization", bearer(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void staffCanReadEvidenceOfAnyReader() throws Exception {
        String readerNo = newAccount("ev");
        createUser(readerNo, "READER", "NORMAL", null);
        String staffNo = newAccount("lib");
        createUser(staffNo, "LIBRARIAN", "NORMAL", null);

        String readerToken = login(readerNo);
        long fileId = upload(readerToken, "evidence.png", "image/png", PNG);

        mockMvc.perform(get("/v1/files/{id}", fileId).header("Authorization", bearer(login(staffNo))))
                .andExpect(status().isOk());
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
        JsonNode node = objectMapper.readTree(body);
        return node.path("data").path("id").asLong();
    }
}
