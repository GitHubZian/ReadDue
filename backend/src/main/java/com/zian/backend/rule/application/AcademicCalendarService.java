package com.zian.backend.rule.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.rule.domain.model.AcademicCalendar;
import com.zian.backend.rule.domain.repository.AcademicCalendarRepository;
import com.zian.backend.rule.interfaces.dto.CalendarDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 校历区间维护：新建 / 修改 / 删除，带学期、场景、起止与重叠校验。
 *
 * <p>只读列表仍由 reporting 的 {@code GET /v1/admin/calendar} 提供，本服务只负责写。
 */
@Service
@RequiredArgsConstructor
public class AcademicCalendarService {

    private static final Set<String> SCENES = Set.of("GRADUATION", "EXAM", "THESIS", "NORMAL");
    private static final Set<String> SOURCES = Set.of("CALENDAR", "DECLARED");
    private static final Pattern TERM = Pattern.compile("\\d{4}-[A-Z0-9]{1,10}");
    private static final int NOTE_MAX = 200;

    private final AcademicCalendarRepository repository;
    private final AuditRecorder auditRecorder;

    /**
     * 全部未删除区间。
     *
     * @return 按开始日期倒序
     */
    public List<CalendarDtos.CalendarView> list() {
        return repository.findAll().stream().map(AcademicCalendarService::view).toList();
    }

    /**
     * 新建区间。
     *
     * @param request 学期/场景/起止
     * @return 新建后的区间
     * @throws BusinessException 参数非法（400）或区间重叠/起止颠倒（422）
     */
    @Transactional
    public CalendarDtos.CalendarView create(CalendarDtos.SaveRequest request) {
        AcademicCalendar calendar = validate(null, request);
        long id = repository.insert(calendar);
        AcademicCalendar saved = repository.findById(id).orElse(calendar);
        auditRecorder.record("CALENDAR_CREATE", "ACADEMIC_CALENDAR", id, "新建校历区间", detail(saved));
        return view(saved);
    }

    /**
     * 修改区间；学期/场景/起止均可改。
     *
     * @param id      主键
     * @param request 新值
     * @return 修改后的区间
     * @throws BusinessException 不存在（404）、参数非法（400）或区间重叠/起止颠倒（422）
     */
    @Transactional
    public CalendarDtos.CalendarView update(long id, CalendarDtos.SaveRequest request) {
        AcademicCalendar existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "校历区间不存在"));
        AcademicCalendar calendar = validate(id, request);
        repository.update(new AcademicCalendar(
                id,
                calendar.term(),
                calendar.sceneCode(),
                calendar.startDate(),
                calendar.endDate(),
                calendar.source(),
                calendar.note()));
        AcademicCalendar saved = repository.findById(id).orElse(existing);
        auditRecorder.record("CALENDAR_UPDATE", "ACADEMIC_CALENDAR", id, "修改校历区间", detail(saved));
        return view(saved);
    }

    /**
     * 软删除区间。
     *
     * @param id 主键
     * @throws BusinessException 不存在（404）
     */
    @Transactional
    public void delete(long id) {
        AcademicCalendar existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "校历区间不存在"));
        repository.softDelete(id, UserContext.requireId());
        auditRecorder.record("CALENDAR_DELETE", "ACADEMIC_CALENDAR", id, "删除校历区间", detail(existing));
    }

    /**
     * 校验并归一化请求；{@code excludeId} 用于修改时排除自身。
     */
    private AcademicCalendar validate(Long excludeId, CalendarDtos.SaveRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
        }
        String term = normalizeTerm(request.term());
        String scene = normalizeScene(request.scene());
        LocalDate start = request.startDate();
        LocalDate end = request.endDate();
        if (start == null || end == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "startDate 与 endDate 不能为空");
        }
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "开始日期不能晚于结束日期");
        }
        String note = request.note() == null ? null : request.note().trim();
        if (note != null && note.length() > NOTE_MAX) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "note 不能超过 " + NOTE_MAX + " 字");
        }
        String source = request.source() == null || request.source().isBlank()
                ? "DECLARED"
                : request.source().trim().toUpperCase(Locale.ROOT);
        if (!SOURCES.contains(source)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "source 只能是 CALENDAR 或 DECLARED");
        }
        if (!repository.findOverlaps(term, scene, start, end, excludeId).isEmpty()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该学期同一场景在所选区间已有校历");
        }
        return new AcademicCalendar(null, term, scene, start, end, source, note);
    }

    private static String normalizeTerm(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "term 不能为空");
        }
        String term = raw.trim().toUpperCase(Locale.ROOT);
        if (!TERM.matcher(term).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "term 格式应为 2026-FALL");
        }
        return term;
    }

    private static String normalizeScene(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scene 不能为空");
        }
        String scene = raw.trim().toUpperCase(Locale.ROOT);
        if (!SCENES.contains(scene)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "scene 只能是 GRADUATION、EXAM、THESIS 或 NORMAL");
        }
        return scene;
    }

    private static Map<String, Object> detail(AcademicCalendar calendar) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("term", calendar.term());
        detail.put("scene", calendar.sceneCode());
        detail.put("startDate", calendar.startDate().toString());
        detail.put("endDate", calendar.endDate().toString());
        detail.put("source", calendar.source());
        return detail;
    }

    private static CalendarDtos.CalendarView view(AcademicCalendar calendar) {
        return new CalendarDtos.CalendarView(
                String.valueOf(calendar.id()),
                calendar.term(),
                calendar.sceneCode(),
                calendar.startDate().toString(),
                calendar.endDate().toString(),
                calendar.source(),
                calendar.note());
    }
}
