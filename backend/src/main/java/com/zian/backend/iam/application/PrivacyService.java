package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import com.zian.backend.iam.domain.model.PrivacyState;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.repository.PrivacyRepository;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.interfaces.dto.PrivacyDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者隐私授权与「立即遗忘」。
 *
 * <p>授权默认最小（两项都关闭）；关闭任一授权即刻记录关闭时间，遗忘只脱敏可识别信息，借阅与罚金等事实保留。
 */
@Service
@RequiredArgsConstructor
public class PrivacyService {

    private static final Logger log = LoggerFactory.getLogger(PrivacyService.class);
    private static final String GRACE_SNAPSHOT_PREFIX = "readdue:grace:snap:";

    private final PrivacyRepository privacyRepository;
    private final UserRepository userRepository;
    private final AuditRecorder auditRecorder;
    private final BorrowOrderRepository borrowOrderRepository;
    private final StringRedisTemplate redisTemplate;

    // region 查询

    /**
     * 读取当前读者的授权状态。
     *
     * @return 授权视图（未建档时返回最小授权）
     */
    public PrivacyDtos.ConsentsView consents() {
        long userId = UserContext.requireId();
        return view(state(userId));
    }

    // endregion

    // region 更新授权

    /**
     * 局部更新授权；关闭任一授权会记录关闭时间。
     *
     * @param request 至少包含 sceneConsent / cacheConsent 之一
     * @return 更新后的授权视图
     */
    @Transactional
    public PrivacyDtos.ConsentsView update(PrivacyDtos.UpdateRequest request) {
        if (request == null || (request.sceneConsent() == null && request.cacheConsent() == null)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少提交一项授权");
        }
        long userId = UserContext.requireId();
        PrivacyState before = state(userId);
        boolean scene = request.sceneConsent() == null ? before.sceneConsent() : request.sceneConsent();
        boolean cache = request.cacheConsent() == null ? before.cacheConsent() : request.cacheConsent();
        LocalDateTime now = LocalDateTime.now();
        boolean revoked = (before.sceneConsent() && !scene) || (before.cacheConsent() && !cache);
        LocalDateTime revokedAt = revoked ? now : before.consentRevokedAt();
        PrivacyState after = new PrivacyState(scene, cache, revokedAt, before.forgetRequestedAt());
        privacyRepository.save(userId, after);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("sceneConsent", scene);
        detail.put("cacheConsent", cache);
        auditRecorder.record(
                revoked ? "CONSENT_REVOKE" : "CONSENT_GRANT",
                "READER_PROFILE",
                userId,
                "读者更新隐私授权",
                detail,
                Map.of("sceneConsent", before.sceneConsent(), "cacheConsent", before.cacheConsent()),
                Map.of("sceneConsent", scene, "cacheConsent", cache));
        return view(after);
    }

    // endregion

    // region 立即遗忘

    /**
     * 立即遗忘：清空姓名、手机号、邮箱与院系/专业/年级，关闭授权并记录遗忘时间。
     *
     * @return 脱敏结果
     */
    @Transactional
    public PrivacyDtos.ForgetResult forget() {
        long userId = UserContext.requireId();
        PrivacyState before = state(userId);
        if (before.forgotten()) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "已提交过遗忘请求");
        }
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号不存在"));
        String displayName = "已注销读者-" + tail(user.getUserNo());
        LocalDateTime now = LocalDateTime.now();
        privacyRepository.anonymize(userId, displayName, now);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("displayName", displayName);
        detail.put("forgetRequestedAt", Times.iso(now));
        Map<String, Object> beforeDetail = new LinkedHashMap<>();
        beforeDetail.put("hasPhone", user.getPhone() != null && !user.getPhone().isBlank());
        beforeDetail.put("hasEmail", user.getEmail() != null && !user.getEmail().isBlank());
        beforeDetail.put("sceneConsent", before.sceneConsent());
        beforeDetail.put("cacheConsent", before.cacheConsent());
        auditRecorder.record(
                "USER_FORGET",
                "SYS_USER",
                userId,
                "读者申请立即遗忘并完成脱敏",
                detail,
                beforeDetail,
                Map.of("anonymous", true));
        evictGraceSnapshots(userId);
        return new PrivacyDtos.ForgetResult(true, displayName, Times.iso(now));
    }

    /**
     * 遗忘后清理该读者借阅单的宽限结果快照，避免缓存残留可识别信息；缓存不可用不影响脱敏结果。
     */
    private void evictGraceSnapshots(long userId) {
        List<Long> orderIds;
        try {
            orderIds = borrowOrderRepository.findIdsByReader(userId);
        } catch (Exception e) {
            log.warn("遗忘清理宽限快照失败：读取借阅单异常 userId={}", userId, e);
            return;
        }
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        List<String> keys = orderIds.stream()
                .filter(Objects::nonNull)
                .map(id -> GRACE_SNAPSHOT_PREFIX + id)
                .toList();
        if (keys.isEmpty()) {
            return;
        }
        try {
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("遗忘清理宽限快照失败 userId={} keys={}", userId, keys, e);
        }
    }

    // endregion

    private PrivacyState state(long userId) {
        return privacyRepository.find(userId).orElse(PrivacyState.minimal());
    }

    private PrivacyDtos.ConsentsView view(PrivacyState state) {
        return new PrivacyDtos.ConsentsView(
                state.sceneConsent(),
                state.cacheConsent(),
                state.consentRevokedAt() == null ? null : Times.iso(state.consentRevokedAt()),
                state.forgotten());
    }

    private static String tail(String userNo) {
        if (userNo == null || userNo.length() <= 4) {
            return "****";
        }
        return userNo.substring(userNo.length() - 4);
    }
}

