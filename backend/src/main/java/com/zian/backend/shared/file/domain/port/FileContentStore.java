package com.zian.backend.shared.file.domain.port;

/**
 * 文件内容存储端口（磁盘、对象存储等实现由 infrastructure 提供）。
 *
 * <p>application 只依赖本接口，便于把本地磁盘换成对象存储而不改业务代码。
 */
public interface FileContentStore {

    /**
     * 写入文件内容。
     *
     * @param fileId    文件 id，可用作文件名
     * @param extension 小写扩展名（png/jpg/pdf）
     * @param content   字节内容
     * @return 相对存储根目录的路径（使用 / 分隔）
     */
    String save(long fileId, String extension, byte[] content);

    /**
     * 读取文件内容。
     *
     * @param relative 保存时返回的相对路径
     * @return 字节内容
     */
    byte[] read(String relative);

    /**
     * 判断文件是否仍存在。
     *
     * @param relative 相对路径
     * @return 存在时为 true
     */
    boolean exists(String relative);

    /**
     * 删除文件内容（遗忘级联清理证据时使用）。
     *
     * @param relative 相对路径
     * @return 实际删除时为 true；文件已不存在返回 false
     */
    boolean delete(String relative);
}
