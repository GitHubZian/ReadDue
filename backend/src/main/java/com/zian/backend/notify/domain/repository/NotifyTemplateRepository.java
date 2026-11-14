package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.NotifyTemplate;
import java.util.List;
import java.util.Optional;

/**
 * 通知模板仓储。
 */
public interface NotifyTemplateRepository {

    /**
     * 按 ID 查模板。
     */

    Optional<NotifyTemplate> findById(Long id);

    /**
     * 列出全部未删除模板。
     */

    List<NotifyTemplate> findAll();

    /**
     * 更新模板正文与启停状态。
     */

    int updateContent(Long id, String titleTpl, String bodyTpl, Boolean enabled);
}
