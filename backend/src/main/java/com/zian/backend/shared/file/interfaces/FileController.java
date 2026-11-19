package com.zian.backend.shared.file.interfaces;

import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import com.zian.backend.shared.file.application.FileService;
import com.zian.backend.shared.file.interfaces.dto.FileDtos;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传与下载（宽限证据等场景复用）。
 */
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件：仅 png/jpg/pdf，单文件不超过 5MB。
     *
     * @param file 表单字段 {@code file}
     * @return 文件 id 与元数据
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<FileDtos.FileView> upload(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(fileService.upload(file));
    }

    /**
     * 下载文件：本人或馆员/管理员可读，其他人按不存在处理。
     *
     * @param id 文件 id
     * @return 文件字节流（附件下载）
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@PathVariable long id) {
        FileDtos.Download file = fileService.download(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.name(), StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(file.content());
    }
}
