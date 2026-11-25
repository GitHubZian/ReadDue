package com.zian.backend.shared.file.application;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.file.domain.model.StoredFile;
import com.zian.backend.shared.file.domain.repository.FileRepository;
import com.zian.backend.shared.file.domain.port.FileContentStore;
import com.zian.backend.shared.file.interfaces.dto.FileDtos;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传与读取：仅接收 png/jpg/pdf、单文件不超过 5MB，按内容魔数校验扩展名。
 *
 * <p>宽限证据等业务只保存文件 id，文件归属与读取权限在本服务统一校验：
 * 本人或馆员/管理员可读，其他人一律按「不存在」处理，避免 id 探测。
 */
@Service
@RequiredArgsConstructor
public class FileService {

    /** 单文件大小上限：5MB。 */
    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> ALLOWED = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "pdf", "application/pdf");

    private final FileRepository fileRepository;
    private final FileContentStore fileStorage;

    /**
     * 上传一个文件并落库元数据。
     *
     * @param file 上传内容
     * @return 文件视图
     */
    @Transactional
    public FileDtos.FileView upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "文件不能超过 5MB");
        }
        String originalName = sanitizeName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String contentType = ALLOWED.get(extension);
        if (contentType == null) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "仅支持 png/jpg/pdf 文件");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("读取上传内容失败", e);
        }
        if (content.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        if (content.length > MAX_BYTES) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "文件不能超过 5MB");
        }
        if (!matchesContent(extension, content)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "文件内容与扩展名不一致");
        }

        long id = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        String storagePath = fileStorage.save(id, "jpeg".equals(extension) ? "jpg" : extension, content);
        StoredFile stored = new StoredFile(
                id,
                UserContext.requireId(),
                originalName,
                contentType,
                content.length,
                sha256(content),
                storagePath,
                LocalDateTime.now());
        fileRepository.insert(stored);
        return FileDtos.FileView.of(stored);
    }

    /**
     * 读取文件元数据，仅本人或馆员可读。
     *
     * @param id 文件 id
     * @return 元数据
     */
    @Transactional(readOnly = true)
    public StoredFile requireReadable(long id) {
        StoredFile file = fileRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在"));
        LoginUser user = UserContext.require();
        if (!user.id().equals(file.ownerId()) && !user.isStaff()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        return file;
    }

    /**
     * 下载文件内容。
     *
     * @param id 文件 id
     * @return 文件名、MIME 与字节内容
     */
    @Transactional(readOnly = true)
    public FileDtos.Download download(long id) {
        StoredFile file = requireReadable(id);
        if (!fileStorage.exists(file.storagePath())) {
            throw new BusinessException(ErrorCode.GONE_ERROR, "文件已失效，请重新上传");
        }
        return new FileDtos.Download(file.originalName(), file.contentType(), fileStorage.read(file.storagePath()));
    }

    /**
     * 校验一组文件都属于指定读者且不超过 5 个，用于宽限证据等业务。
     *
     * @param ids     文件 id 列表（可重复，按首次出现去重）
     * @param ownerId 期望的归属人
     * @return 去重后的文件元数据（顺序与入参一致）
     */
    @Transactional(readOnly = true)
    public List<StoredFile> requireOwned(Collection<Long> ids, long ownerId) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids);
        if (distinct.size() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多上传 5 个证据附件");
        }
        List<StoredFile> files = fileRepository.findByIds(distinct);
        if (files.size() != distinct.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "证据附件不存在");
        }
        for (StoredFile file : files) {
            if (!file.ownerId().equals(ownerId)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "证据附件不存在");
            }
        }
        return files;
    }

    /**
     * 批量读取元数据（不做归属校验，仅供已校验过归属的业务展示附件）。
     *
     * @param ids 文件 id 集合
     * @return 命中的元数据
     */
    @Transactional(readOnly = true)
    public List<StoredFile> files(Collection<Long> ids) {
        return fileRepository.findByIds(ids);
    }

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name;
        int slash = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slash >= 0) {
            value = value.substring(slash + 1);
        }
        value = value.replaceAll("[\\p{Cntrl}]", "").trim();
        if (value.isEmpty()) {
            value = "evidence";
        }
        return value.length() > 200 ? value.substring(0, 200) : value;
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 || dot == name.length() - 1
                ? ""
                : name.substring(dot + 1).toLowerCase();
    }

    private static boolean matchesContent(String extension, byte[] bytes) {
        return switch (extension) {
            case "png" -> isDecodableImage(bytes, "png");
            case "jpg", "jpeg" -> isDecodableImage(bytes, "jpeg");
            case "pdf" -> bytes.length >= 5
                    && bytes[0] == '%'
                    && bytes[1] == 'P'
                    && bytes[2] == 'D'
                    && bytes[3] == 'F'
                    && bytes[4] == '-';
            default -> false;
        };
    }

    /**
     * 真实解码校验：仅靠文件头会被 12 字节伪 PNG 绕过；这里要求 ImageIO 能读出
     * 宽高，并校验实际格式与扩展名一致（png / jpeg）。
     */
    private static boolean isDecodableImage(byte[] bytes, String expectedFormat) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                return false;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return false;
            }
            ImageReader reader = readers.next();
            try {
                if (!expectedFormat.equalsIgnoreCase(reader.getFormatName())) {
                    return false;
                }
                reader.setInput(input, true, true);
                return reader.getWidth(0) > 0 && reader.getHeight(0) > 0;
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM 缺少 SHA-256", e);
        }
    }
}
