package com.zian.backend.notify.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zian.backend.notify.domain.model.NotifyTemplate;
import com.zian.backend.notify.domain.repository.NotifyTemplateRepository;
import com.zian.backend.notify.infrastructure.persistence.mapper.NotifyTemplateMapper;
import com.zian.backend.notify.infrastructure.persistence.po.NotifyTemplatePO;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 通知模板仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class NotifyTemplateRepositoryImpl implements NotifyTemplateRepository {

    private final NotifyTemplateMapper mapper;

    @Override
    public Optional<NotifyTemplate> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<NotifyTemplate> findAll() {
        return mapper.selectList(Wrappers.<NotifyTemplatePO>lambdaQuery()
                        .eq(NotifyTemplatePO::getDeleted, 0L)
                        .orderByAsc(NotifyTemplatePO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int updateContent(Long id, String titleTpl, String bodyTpl, Boolean enabled) {
        NotifyTemplatePO po = new NotifyTemplatePO();
        po.setId(id);
        po.setTitleTpl(titleTpl);
        po.setBodyTpl(bodyTpl);
        po.setEnabled(enabled == null ? null : (enabled ? 1 : 0));
        return mapper.updateById(po);
    }

    private NotifyTemplate toDomain(NotifyTemplatePO po) {
        return new NotifyTemplate(
                po.getId(),
                po.getTemplateCode(),
                po.getChannel(),
                po.getTitleTpl(),
                po.getBodyTpl(),
                po.getToneCode(),
                po.getVersion(),
                po.getEnabled() != null && po.getEnabled() == 1,
                po.getUpdatedAt());
    }
}
