package com.zian.backend.audit.application;

import com.zian.backend.audit.domain.model.AuditRecord;
import com.zian.backend.audit.domain.repository.AuditLogRepository;
import com.zian.backend.shared.common.TraceContext;
import com.zian.backend.shared.common.WebUtils;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计记录入口：各上下文在写操作后调用，自动补齐操作者、IP 与 traceId。
 *
 * <p>需要留痕的写操作使用六参重载传入 {@code before}/{@code after}；
 * 其余场景调用四参/五参重载即可，前后值落 NULL。
 */
@Service
@RequiredArgsConstructor
public class AuditRecorder {

    private final AuditLogRepository repository;

    public void record(String action, String objectType, Long objectId, String purpose) {
        record(action, objectType, objectId, purpose, Collections.emptyMap());
    }

    public void record(
            String action, String objectType, Long objectId, String purpose, Map<String, Object> detail) {
        record(action, objectType, objectId, purpose, detail, null, null);
    }

    /**
     * 记录带变更前后值的审计（宽限审批、申诉处理、规则调整等需要复盘的写操作）。
     *
     * @param action     动作码，如 {@code GRACE_APPROVE}
     * @param objectType 对象类型
     * @param objectId   对象 ID，可为 null
     * @param purpose    追责用途标签
     * @param detail     附加明细，可为 null
     * @param before     变更前快照（只放标量），可为 null
     * @param after      变更后快照（只放标量），可为 null
     */
    public void record(
            String action,
            String objectType,
            Long objectId,
            String purpose,
            Map<String, Object> detail,
            Map<String, Object> before,
            Map<String, Object> after) {
        LoginUser actor = UserContext.current();
        append(actor, action, objectType, objectId, purpose, detail, before, after);
    }

    /** 显式指定操作者（如登录时尚未建立 UserContext）。 */
    public void recordAs(
            LoginUser actor,
            String action,
            String objectType,
            Long objectId,
            String purpose,
            Map<String, Object> detail) {
        recordAs(actor, action, objectType, objectId, purpose, detail, null, null);
    }

    /** 显式指定操作者并留痕前后值（如 SLA 定时任务）。 */
    public void recordAs(
            LoginUser actor,
            String action,
            String objectType,
            Long objectId,
            String purpose,
            Map<String, Object> detail,
            Map<String, Object> before,
            Map<String, Object> after) {
        append(actor, action, objectType, objectId, purpose, detail, before, after);
    }

    private void append(
            LoginUser actor,
            String action,
            String objectType,
            Long objectId,
            String purpose,
            Map<String, Object> detail,
            Map<String, Object> before,
            Map<String, Object> after) {
        repository.append(AuditRecord.of(
                actor == null ? null : actor.id(),
                actor == null ? null : actor.role(),
                action,
                objectType,
                objectId,
                purpose,
                detail == null ? Map.of() : detail,
                currentIp(),
                TraceContext.current(),
                before,
                after));
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return WebUtils.getClientIp(request);
        }
        return null;
    }
}
