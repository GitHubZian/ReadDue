package com.zian.backend.notify.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.notify.domain.model.NotifyTemplate;
import com.zian.backend.notify.domain.repository.NotifyTemplateRepository;
import com.zian.backend.notify.interfaces.dto.NotifyDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知模板应用服务：话术维护与展示映射（列表读模型见 reporting）。
 */
@Service
@RequiredArgsConstructor
public class NotifyTemplateService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{(\\w+)}}");
    private static final Map<String, String> NAMES = Map.of(
            "DUE_L1", "到期前 3 天提醒",
            "DUE_L2", "到期当天提醒",
            "DUE_L3", "逾期提醒",
            "GRACE_RESULT", "宽限结果通知",
            "APPEAL_SLA", "申诉受理通知",
            "RESERVATION_READY", "预约到书通知");

    private final NotifyTemplateRepository repository;
    private final AuditRecorder auditRecorder;

    /**
     * 更新模板正文与启停状态。
     *
     * @param id    模板 ID
     * @param patch 模板补丁
     * @return 更新后的模板视图
     */
    @Transactional
    public NotifyDtos.TemplateView update(Long id, NotifyDtos.TemplatePatch patch) {
        NotifyTemplate current =
                repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "模板不存在"));
        String title = current.titleTpl();
        String body = patch == null || patch.content() == null ? current.bodyTpl() : patch.content();
        Boolean enabled = patch == null ? null : patch.enabled();
        if (repository.updateContent(id, title, body, enabled) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "模板已被其他人修改，请刷新后重试");
        }
        auditRecorder.record("NOTIFY_TEMPLATE_UPDATE", "NOTIFY_TEMPLATE", id, "维护通知话术", Map.of());
        return view(repository.findById(id).orElseThrow());
    }

    /**
     * 把模板实体转换为前端视图（渠道与场景做枚举映射）。
     *
     * @param template 模板实体
     * @return 模板视图
     */
    public NotifyDtos.TemplateView view(NotifyTemplate template) {
        return new NotifyDtos.TemplateView(
                String.valueOf(template.id()),
                NAMES.getOrDefault(template.templateCode(), template.templateCode()),
                channel(template.channel()),
                scene(template.templateCode()),
                template.enabled(),
                template.bodyTpl(),
                variables(template.bodyTpl()),
                template.updatedAt() == null ? null : template.updatedAt().atZone(ZONE).toOffsetDateTime().toString());
    }

    /**
     * 库内渠道码转前端渠道（INBOX→INAPP、APP_PUSH→WECHAT）。
     *
     * @param dbChannel 库内渠道
     * @return 前端渠道码
     */
    public static String channel(String dbChannel) {
        return switch (dbChannel) {
            case "INBOX" -> "INAPP";
            case "APP_PUSH" -> "WECHAT";
            default -> dbChannel;
        };
    }

    /**
     * 由模板编码推导场景（RETURN/GRACE/APPEAL/RESERVATION/SYSTEM）。
     *
     * @param templateCode 模板编码
     * @return 场景码
     */
    public static String scene(String templateCode) {
        if (templateCode.startsWith("DUE_")) {
            return "RETURN";
        }
        if (templateCode.startsWith("GRACE")) {
            return "GRACE";
        }
        if (templateCode.startsWith("APPEAL")) {
            return "APPEAL";
        }
        if (templateCode.startsWith("RESERVATION")) {
            return "RESERVATION";
        }
        return "SYSTEM";
    }

    private List<String> variables(String content) {
        List<String> result = new ArrayList<>();
        Matcher matcher = VARIABLE.matcher(content == null ? "" : content);
        while (matcher.find()) {
            if (!result.contains(matcher.group(1))) {
                result.add(matcher.group(1));
            }
        }
        return result;
    }
}

