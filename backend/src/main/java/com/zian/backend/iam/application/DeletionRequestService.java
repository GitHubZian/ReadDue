package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.iam.domain.model.DeletionRequest;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.repository.DeletionRequestRepository;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.interfaces.dto.DeletionDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者注销申请（M6）：读者提交、管理员批准/驳回；批准复用「立即遗忘」的脱敏与级联清理。
 */
@Service
@RequiredArgsConstructor
public class DeletionRequestService {

    private static final int MAX_LIST = 200;

    private final DeletionRequestRepository repository;
    private final UserRepository userRepository;
    private final PrivacyService privacyService;
    private final AuditRecorder auditRecorder;

    /**
     * 提交注销申请：已有待处理申请或账号已注销时冲突。
     *
     * @param request 申请原因，可空
     * @return 申请视图
     */
    @Transactional
    public DeletionDtos.View create(DeletionDtos.CreateRequest request) {
        long userId = UserContext.requireId();
        if (privacyService.forgotten(userId)) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "账号已注销");
        }
        if (repository.findPending(userId).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "已有待处理的注销申请");
        }
        String reason = request == null ? null : trimToNull(request.reason());
        long id = repository.insert(new DeletionRequest(
                null, userId, "PENDING", reason, null, LocalDateTime.now(), null, null));
        auditRecorder.record(
                "DELETION_REQUEST_CREATE",
                "DELETION_REQUEST",
                id,
                "读者提交注销申请",
                reason == null ? Map.of() : Map.of("reason", reason));
        return view(requireById(id));
    }

    /**
     * 当前读者最近一次注销申请。
     *
     * @return 申请视图；从未申请时为 null
     */
    public DeletionDtos.View mine() {
        return repository.findLatest(UserContext.requireId()).map(this::view).orElse(null);
    }

    /**
     * 管理端申请列表，按提交时间倒序。
     *
     * @param status 状态过滤，空表示全部
     * @param limit  条数上限
     * @return 申请列表
     */
    public List<DeletionDtos.View> list(String status, int limit) {
        int size = Math.max(1, Math.min(limit, MAX_LIST));
        String mapped = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        return repository.list(mapped, size).stream().map(this::view).toList();
    }

    /**
     * 批准注销：对目标读者执行遗忘脱敏与级联清理，随后标记 APPROVED。
     *
     * @param id 申请 ID
     * @return 处理后的申请
     */
    @Transactional
    public DeletionDtos.View approve(long id) {
        DeletionRequest request = requirePending(id);
        if (!privacyService.forgotten(request.userId())) {
            privacyService.forgetFor(request.userId());
        }
        finish(id, "APPROVED", request.note(), "DELETION_REQUEST_APPROVE", "管理员批准注销申请");
        return view(requireById(id));
    }

    /**
     * 驳回注销申请。
     *
     * @param id      申请 ID
     * @param request 处理备注，可空
     * @return 处理后的申请
     */
    @Transactional
    public DeletionDtos.View reject(long id, DeletionDtos.RejectRequest request) {
        requirePending(id);
        String note = request == null ? null : trimToNull(request.note());
        finish(id, "REJECTED", note, "DELETION_REQUEST_REJECT", "管理员驳回注销申请");
        return view(requireById(id));
    }

    private void finish(long id, String status, String note, String action, String purpose) {
        repository.updateStatus(id, status, note, UserContext.requireId(), LocalDateTime.now());
        auditRecorder.record(action, "DELETION_REQUEST", id, purpose, Map.of("status", status));
    }

    private DeletionRequest requirePending(long id) {
        DeletionRequest request =
                repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "注销申请不存在"));
        if (!"PENDING".equals(request.status())) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "该申请已处理");
        }
        return request;
    }

    private DeletionRequest requireById(long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "注销申请不存在"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private DeletionDtos.View view(DeletionRequest request) {
        User reader = userRepository.findById(request.userId()).orElse(null);
        return new DeletionDtos.View(
                String.valueOf(request.id()),
                String.valueOf(request.userId()),
                reader == null ? null : reader.getName(),
                reader == null ? null : reader.getUserNo(),
                request.status(),
                request.reason(),
                request.note(),
                Times.iso(request.createdAt()),
                Times.iso(request.handledAt()),
                request.handledBy() == null ? null : String.valueOf(request.handledBy()));
    }
}
