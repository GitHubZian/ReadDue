package com.zian.backend.notify.application;

import com.zian.backend.notify.domain.model.QuietHours;
import com.zian.backend.notify.domain.repository.QuietHoursRepository;
import com.zian.backend.notify.interfaces.dto.QuietHoursDtos;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者免打扰时段：读取、局部更新，以及排程时使用「读者窗口优先、全局规则兜底」。
 */
@Service
@RequiredArgsConstructor
public class QuietHoursService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final String SOURCE_CUSTOM = "CUSTOM";
    private static final String SOURCE_FALLBACK = "FALLBACK";

    private final QuietHoursRepository quietHoursRepository;
    private final RuleService ruleService;

    /**
     * 当前读者的免打扰时段。
     *
     * @return 时段视图，未自定义时返回全局兜底值
     */
    public QuietHoursDtos.QuietHoursView view() {
        return view(effective(UserContext.requireId()));
    }

    /**
     * 局部更新免打扰时段。
     *
     * @param request 更新请求，至少包含一个字段
     * @return 更新后的时段
     */
    @Transactional
    public QuietHoursDtos.QuietHoursView update(QuietHoursDtos.UpdateRequest request) {
        if (request == null || (request.enabled() == null && request.start() == null && request.end() == null)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少指定 enabled、start 或 end 其中一个");
        }
        long userId = UserContext.requireId();
        QuietHours current = effective(userId);
        QuietHours next = new QuietHours(
                request.enabled() == null ? current.enabled() : request.enabled(),
                request.start() == null ? current.start() : parse(request.start(), "start"),
                request.end() == null ? current.end() : parse(request.end(), "end"),
                true);
        validate(next);
        quietHoursRepository.save(userId, next);
        return view(next);
    }

    /**
     * 排程使用的有效窗口：读者自定义优先，否则取全局规则 `QUIET.START`/`QUIET.END`。
     *
     * @param userId 读者用户 ID
     * @return 有效时段
     */
    public QuietHours effective(long userId) {
        return quietHoursRepository.find(userId).orElseGet(() -> QuietHours.fallback(
                LocalTime.parse(ruleService.text("QUIET.START", "22:00")),
                LocalTime.parse(ruleService.text("QUIET.END", "08:00"))));
    }

    /**
     * 校验：起止不能相同（同值会让 `[start, end)` 变成空集，读者无法预期）。
     *
     * @param quietHours 待校验时段
     */
    private void validate(QuietHours quietHours) {
        if (quietHours.start().equals(quietHours.end())) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "免打扰开始与结束不能相同");
        }
    }

    private LocalTime parse(String raw, String field) {
        try {
            return LocalTime.parse(raw.trim(), HH_MM);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, field + " 需为 HH:mm 格式");
        }
    }

    private QuietHoursDtos.QuietHoursView view(QuietHours quietHours) {
        return new QuietHoursDtos.QuietHoursView(
                quietHours.enabled(),
                quietHours.start().format(HH_MM),
                quietHours.end().format(HH_MM),
                quietHours.customized() ? SOURCE_CUSTOM : SOURCE_FALLBACK);
    }
}
