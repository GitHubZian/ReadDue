package com.zian.backend.rule.interfaces;

import com.zian.backend.rule.application.RuleService;
import com.zian.backend.rule.interfaces.dto.RuleDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 规则写接口（读模型在 reporting）。
 */
@RestController
@RequestMapping("/v1/admin/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @PutMapping
    @PreAuthorize("hasAuthority('rule:update')")
    public BaseResponse<RuleDtos.RuleUpdateResult> update(@RequestBody @Valid RuleDtos.RuleUpdateRequest request) {
        return ResultUtils.success(new RuleDtos.RuleUpdateResult(ruleService.update(request)));
    }
}

