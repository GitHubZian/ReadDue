package com.zian.backend.shared.file.infrastructure;

import com.zian.backend.shared.file.domain.port.FileContentStore;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 本地磁盘文件存储：按 {@code yyyy/MM/<fileId>.<ext>} 落盘，数据库只保存相对路径。
 *
 * <p>当前部署为单机，路径可经 {@code app.file.storage-dir}（或环境变量 {@code FILE_STORAGE_DIR}）覆盖；
 * 换成对象存储时只需替换本类，应用层不感知。
 */
@Component
public class FileStorage implements FileContentStore {

    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);
    private static final DateTimeFormatter MONTH_DIR = DateTimeFormatter.ofPattern("yyyy/MM");

    private final Path root;

    public FileStorage(@Value("${app.file.storage-dir:${java.io.tmpdir}/readdue/uploads}") String storageDir) {
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    /** 启动时确保根目录存在，缺失即创建；失败直接让启动失败，避免运行期才发现无法落盘。 */
    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
            log.info("文件存储目录：{}", root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建文件存储目录：" + root, e);
        }
    }

    /**
     * 写入文件内容。
     *
     * @param fileId    文件 id，用作文件名
     * @param extension 小写扩展名（png/jpg/pdf）
     * @param content   字节内容
     * @return 相对存储根目录的路径（使用 / 分隔，便于跨平台落库）
     */
    public String save(long fileId, String extension, byte[] content) {
        String relative = LocalDate.now().format(MONTH_DIR) + "/" + fileId + "." + extension;
        Path target = resolve(relative);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return relative;
        } catch (IOException e) {
            throw new UncheckedIOException("文件写入失败：" + relative, e);
        }
    }

    /**
     * 读取文件内容。
     *
     * @param relative 保存时返回的相对路径
     * @return 字节内容
     */
    public byte[] read(String relative) {
        Path target = resolve(relative);
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new UncheckedIOException("文件读取失败：" + relative, e);
        }
    }

    /**
     * 判断存储路径是否仍存在（用于提示附件已丢失）。
     *
     * @param relative 相对路径
     * @return 文件存在时为 true
     */
    public boolean exists(String relative) {
        return Files.isRegularFile(resolve(relative));
    }

    private Path resolve(String relative) {
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法的文件路径：" + relative);
        }
        return target;
    }
}
