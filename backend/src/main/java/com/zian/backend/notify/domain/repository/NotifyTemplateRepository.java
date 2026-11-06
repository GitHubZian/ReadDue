package com.zian.backend.notify.domain.repository;

import com.zian.backend.notify.domain.model.NotifyTemplate;
import java.util.List;
import java.util.Optional;

/**
 * 通知模板仓储。
 */
public interface NotifyTemplateRepository {

    Optional<NotifyTemplate> findById(Long id);

    List<NotifyTemplate> findAll();

    int updateContent(Long id, String titleTpl, String bodyTpl, Boolean enabled);
}
