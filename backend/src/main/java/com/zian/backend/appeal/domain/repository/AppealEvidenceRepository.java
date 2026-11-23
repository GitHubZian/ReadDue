package com.zian.backend.appeal.domain.repository;

import java.util.List;
import java.util.Map;

/**
 * 申诉工单证据附件关联（B-09）：只保存文件 id，文件本体由 {@code shared/file} 管理。
 */
public interface AppealEvidenceRepository {

    /**
     * 为一次申诉追加附件关联（调用方已校验文件归属与数量）。
     *
     * @param appealId 申诉工单 id
     * @param fileIds  文件 id 列表
     */
    void attach(long appealId, List<Long> fileIds);

    /**
     * 查询一次申诉的附件 id（按关联时间升序）。
     *
     * @param appealId 申诉工单 id
     * @return 文件 id 列表
     */
    List<Long> fileIdsOf(long appealId);

    /**
     * 批量查询多次申诉的附件，用于列表页避免 N+1。
     *
     * @param appealIds 申诉工单 id 集合
     * @return appealId → 文件 id 列表（无附件的工单不出现在 map 中）
     */
    Map<Long, List<Long>> fileIdsOf(List<Long> appealIds);
}
