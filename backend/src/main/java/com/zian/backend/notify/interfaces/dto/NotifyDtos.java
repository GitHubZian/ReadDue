package com.zian.backend.notify.interfaces.dto;

import java.util.List;

/**
 * 通知模板接口模型。
 */
public final class NotifyDtos {

    private NotifyDtos() {
    }

    public record TemplatePatch(String name, String channel, String scene, Boolean enabled, String content) {
    }

    public record TemplateView(
            String id,
            String name,
            String channel,
            String scene,
            boolean enabled,
            String content,
            List<String> variables,
            String updatedAt) {
    }

    public record RemindRequest(List<String> ids) {
    }

    public record RemindResult(int sent) {
    }
}
