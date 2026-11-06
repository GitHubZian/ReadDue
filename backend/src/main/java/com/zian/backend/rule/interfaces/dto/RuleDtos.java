package com.zian.backend.rule.interfaces.dto;

import java.util.List;

/**
 * 规则读写 DTO（前端 RuleSet / RuleGroup / RuleItem 的入参子集）。
 */
public final class RuleDtos {

    private RuleDtos() {
    }

    public record RuleItem(String key, String label, String description, String type, String value, String unit) {
    }

    public record RuleGroup(String id, String title, String description, List<RuleItem> items) {
    }

    public record RuleUpdateRequest(List<RuleGroup> groups, @jakarta.validation.constraints.NotBlank String expectedVersion) {
    }

    public record RuleUpdateResult(String version) {
    }
}

