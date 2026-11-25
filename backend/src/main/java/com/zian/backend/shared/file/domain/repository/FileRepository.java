package com.zian.backend.shared.file.domain.repository;

import com.zian.backend.shared.file.domain.model.StoredFile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 文件元数据仓储（磁盘内容由 infrastructure 的存储组件负责）。
 */
public interface FileRepository {

    /**
     * 写入一条文件元数据。
     *
     * @param file 元数据（id 由调用方生成）
     */
    void insert(StoredFile file);

    /**
     * 按 id 查询。
     *
     * @param id 文件 id
     * @return 存在时返回元数据
     */
    Optional<StoredFile> findById(long id);

    /**
     * 批量按 id 查询，顺序不保证。
     *
     * @param ids 文件 id 集合
     * @return 命中的元数据
     */
    List<StoredFile> findByIds(Collection<Long> ids);

    /**
     * 把一批文件标记为已删除（保留元数据行，下载与证据展示按不存在处理）。
     *
     * @param ids 文件 id 集合
     * @return 影响行数
     */
    int markDeleted(Collection<Long> ids);
}
