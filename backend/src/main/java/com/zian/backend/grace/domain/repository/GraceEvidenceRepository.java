package com.zian.backend.grace.domain.repository;

import java.util.List;
import java.util.Map;

/**
 * 宽限申请证据附件关联（B-08）：只保存文件 id，文件本体由 {@code shared/file} 管理。
 */
public interface GraceEvidenceRepository {

    /**
     * 为一次申请追加附件关联（调用方已校验文件归属与数量）。
     *
     * @param graceId 宽限申请 id
     * @param fileIds 文件 id 列表
     */
    void attach(long graceId, List<Long> fileIds);

    /**
     * 查询一次申请的附件 id（按关联时间升序）。
     *
     * @param graceId 宽限申请 id
     * @return 文件 id 列表
     */
    List<Long> fileIdsOf(long graceId);

    /**
     * 批量查询多次申请的附件，用于列表页避免 N+1。
     *
     * @param graceIds 宽限申请 id 集合
     * @return graceId → 文件 id 列表（无附件的申请不出现在 map 中）
     */
    Map<Long, List<Long>> fileIdsOf(List<Long> graceIds);
}
