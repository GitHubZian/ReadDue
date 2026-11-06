package com.zian.backend.audit.application;

import com.zian.backend.audit.domain.model.AuditRecord;
import com.zian.backend.audit.domain.repository.AuditLogRepository;
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
 * 审计记录入口：各上下文在写操作后调用，自动补齐操作者与 IP。
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
        LoginUser actor = UserContext.current();
        repository.append(AuditRecord.of(
                actor == null ? null : actor.id(),
                actor == null ? null : actor.role(),
                action,
                objectType,
                objectId,
                purpose,
                detail == null ? Map.of() : detail,
                currentIp()));
    }

    /** 显式指定操作者（如登录时尚未建立 UserContext）。 */
    public void recordAs(
            LoginUser actor,
            String action,
            String objectType,
            Long objectId,
            String purpose,
            Map<String, Object> detail) {
        repository.append(AuditRecord.of(
                actor == null ? null : actor.id(),
                actor == null ? null : actor.role(),
                action,
                objectType,
                objectId,
                purpose,
                detail == null ? Map.of() : detail,
                currentIp()));
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return WebUtils.getClientIp(request);
        }
        return null;
    }
}
