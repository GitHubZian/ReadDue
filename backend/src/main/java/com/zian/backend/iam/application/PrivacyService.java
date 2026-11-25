package com.zian.backend.iam.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import com.zian.backend.grace.domain.repository.GraceEvidenceRepository;
import com.zian.backend.iam.domain.model.PrivacyState;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.repository.PrivacyRepository;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.interfaces.dto.PrivacyDtos;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.file.application.FileService;
import com.zian.backend.shared.security.JwtService;
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
    private final GraceEvidenceRepository graceEvidenceRepository;
    private final FileService fileService;
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

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

    /**
     * 场景声明授权是否开启（宽限场景、课表读取的前置闸门）。
     *
     * @param userId 读者账号
     * @return 已授权时为 true
     */
    public boolean sceneConsent(long userId) {
        return state(userId).sceneConsent();
    }

    /**
     * 宽限计算缓存授权是否开启。
     *
     * @param userId 读者账号
     * @return 已授权时为 true
     */
    public boolean cacheConsent(long userId) {
        return state(userId).cacheConsent();
    }

    /**
     * 指定读者是否已完成遗忘。
     *
     * @param userId 读者账号
     * @return 已遗忘时为 true
     */
    public boolean forgotten(long userId) {
        return state(userId).forgotten();
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
        // 关闭缓存授权立即遗忘：删除该读者全部宽限结果快照，避免继续复用可识别信息。
        if (before.cacheConsent() && !cache) {
            evictGraceSnapshots(userId);
        }
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
        return forgetFor(UserContext.requireId());
    }

    /**
     * 代指定读者执行遗忘（管理端批准注销申请时使用；审计操作者仍为当前管理员）。
     *
     * @param userId 目标读者
     * @return 脱敏结果
     */
    @Transactional
    public PrivacyDtos.ForgetResult forgetFor(long userId) {
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

        // 遗忘级联：先清缓存快照，再删该读者宽限证据文件（元数据逻辑删除 + 物理内容删除）。
        int removedSnapshots = evictGraceSnapshots(userId);
        int removedFiles = removeEvidenceFiles(userId);
        // 注销即终止登录态：删除该用户全部 jti 绑定，旧 token 立即失效。
        int revokedTokens = jwtService.revokeAllFor(userId);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("displayName", displayName);
        detail.put("forgetRequestedAt", Times.iso(now));
        detail.put("removedSnapshots", removedSnapshots);
        detail.put("removedFiles", removedFiles);
        detail.put("revokedTokens", revokedTokens);
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
        return new PrivacyDtos.ForgetResult(true, displayName, Times.iso(now));
    }

    /**
     * 遗忘/关闭缓存授权后清理该读者借阅单的宽限结果快照，避免缓存残留可识别信息；缓存不可用不影响主流程。
     *
     * @return 实际尝试删除的键数量
     */
    private int evictGraceSnapshots(long userId) {
        List<Long> orderIds;
        try {
            orderIds = borrowOrderRepository.findIdsByReader(userId);
        } catch (Exception e) {
            log.warn("遗忘清理宽限快照失败：读取借阅单异常 userId={}", userId, e);
            return 0;
        }
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }
        List<String> keys = orderIds.stream()
                .filter(Objects::nonNull)
                .map(id -> GRACE_SNAPSHOT_PREFIX + id)
                .toList();
        if (keys.isEmpty()) {
            return 0;
        }
        try {
            Long removed = redisTemplate.delete(keys);
            return removed == null ? 0 : removed.intValue();
        } catch (Exception e) {
            log.warn("遗忘清理宽限快照失败 userId={} keys={}", userId, keys, e);
            return 0;
        }
    }

    /**
     * 遗忘级联删除该读者提交过的宽限证据：元数据逻辑删除、物理文件删除、关联行清理。
     *
     * <p>任一步失败只记录告警，不阻断已经完成的账号脱敏（遗忘是读者强诉求，宁可残留文件也不能回滚脱敏）。
     *
     * @return 实际删除的物理文件数量
     */
    private int removeEvidenceFiles(long userId) {
        List<Long> fileIds;
        try {
            fileIds = graceEvidenceRepository.fileIdsOfReader(userId);
        } catch (Exception e) {
            log.warn("遗忘清理证据文件失败：读取证据异常 userId={}", userId, e);
            return 0;
        }
        if (fileIds == null || fileIds.isEmpty()) {
            return 0;
        }
        int removed = 0;
        try {
            removed = fileService.deleteEvidenceFiles(fileIds);
        } catch (Exception e) {
            log.warn("遗忘清理证据文件失败：删除文件异常 userId={} fileIds={}", userId, fileIds, e);
        }
        try {
            graceEvidenceRepository.deleteByFileIds(fileIds);
        } catch (Exception e) {
            log.warn("遗忘清理证据关联失败 userId={} fileIds={}", userId, fileIds, e);
        }
        return removed;
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

