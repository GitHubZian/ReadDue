package com.zian.backend.grace.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.application.BorrowService;
import com.zian.backend.borrow.domain.model.BorrowOrder;
import com.zian.backend.grace.domain.model.GraceApplication;
import com.zian.backend.grace.domain.model.GraceCalculator;
import com.zian.backend.grace.domain.model.GraceStatus;
import com.zian.backend.grace.domain.repository.GraceApplicationRepository;
import com.zian.backend.grace.domain.repository.GraceEvidenceRepository;
import com.zian.backend.grace.interfaces.dto.GraceDtos;
import com.zian.backend.iam.application.ReaderCreditService;
import com.zian.backend.iam.application.UserQueryService;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.reservation.application.ReservationService;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.Times;
import com.zian.backend.shared.constant.MqConstant;
import com.zian.backend.shared.exception.BusinessException;
import com.zian.backend.shared.file.domain.model.StoredFile;
import com.zian.backend.shared.file.application.FileService;
import com.zian.backend.shared.outbox.application.OutboxAppender;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宽限应用服务：读者提交申请（确定性公式快照）、管理员审批、批准后延长有效应还日期。
 */
@Service
@RequiredArgsConstructor
public class GraceService {

    private static final Set<String> SCENES = Set.of("NORMAL", "EXAM", "THESIS", "GRADUATION");
    private static final String SNAPSHOT_PREFIX = "readdue:grace:snap:";
    private static final DateTimeFormatter APPLY_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final GraceApplicationRepository repository;
    private final GraceEvidenceRepository evidenceRepository;
    private final FileService fileService;
    private final BorrowService borrowService;
    private final ReservationService reservationService;
    private final ReaderCreditService readerCreditService;
    private final UserQueryService userQueryService;
    private final RuleService ruleService;
    private final OutboxAppender outboxAppender;
    private final AuditRecorder auditRecorder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 申请宽限：按场景基准、信用档位与预约队列计算天数，落库为待审批。
     *
     * @param request 借阅单、场景与说明
     * @return 申请视图
     */
    @Transactional
    public GraceDtos.ApplicationView apply(GraceDtos.ApplyRequest request) {
        Long readerId = UserContext.requireId();
        User reader = userQueryService.requireById(readerId);
        reader.assertCanBorrow("申请宽限");
        BorrowOrder order = borrowService.requireOrder(request.borrowOrderId());
        if (!readerId.equals(order.getReaderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只能为自己的借阅申请宽限");
        }
        if (!order.isActive()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该借阅已结束，无法申请宽限");
        }
        if (repository.existsPending(order.getId())) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该借阅已有待审批的宽限申请");
        }
        LocalDateTime now = LocalDateTime.now();
        if (repository.countApprovedSince(order.getId(), now.minusDays(30)) >= 2) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "同一借阅 30 天内最多批准 2 次宽限");
        }
        List<StoredFile> evidence = fileService.requireOwned(request.evidenceIds(), readerId);

        String scene = request.sceneCode() == null || request.sceneCode().isBlank()
                ? "NORMAL"
                : request.sceneCode().trim().toUpperCase();
        if (!SCENES.contains(scene)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "场景只能是 NORMAL/EXAM/THESIS/GRADUATION");
        }
        int baseDays = ruleService.intValue("BS." + scene, 3);
        int dmax = ruleService.intValue("GRACE.DMAX", 14);
        BigDecimal step = ruleService.decimal("GRACE.ROUND_STEP", "0.5");

        int tier = readerCreditService.ktTier(readerId);
        BigDecimal kt = ruleService.decimal("KT.TIER_" + tier, "1.00");
        int queueLen = reservationService.countWaiting(order.getBookId());
        int level = queueLen == 0 ? 0 : queueLen < 4 ? 1 : 2;
        BigDecimal kq = ruleService.decimal("KQ.LEVEL_" + level, "1.00");

        BigDecimal computed = GraceCalculator.compute(BigDecimal.valueOf(baseDays), kt, kq, dmax, step);
        LocalDateTime snapshotExpiresAt =
                now.plusHours(ruleService.intValue("GRACE.SNAPSHOT_TTL_HOURS", 24));

        GraceApplication application = new GraceApplication(
                null,
                nextApplyNo(now),
                order.getId(),
                readerId,
                scene,
                "DECLARED",
                false,
                BigDecimal.valueOf(baseDays),
                kt,
                kq,
                dmax,
                computed,
                null,
                request.evidenceText() == null ? "" : request.evidenceText().trim(),
                "v1",
                queueLen,
                snapshotExpiresAt,
                GraceStatus.PENDING,
                null,
                null,
                null,
                0);
        Long id = repository.insert(application);
        // 仓储只返回主键，回填到聚合后再复用统一的视图转换（含证据附件）
        application.setId(id);
        evidenceRepository.attach(id, evidence.stream().map(StoredFile::id).toList());
        cacheSnapshot(order.getId(), application, snapshotExpiresAt);
        auditRecorder.record(
                "GRACE_APPLY",
                "GRACE_APPLICATION",
                id,
                "提交宽限申请",
                Map.of(
                        "orderId", order.getId(),
                        "scene", scene,
                        "computedDays", computed.toPlainString(),
                        "evidenceCount", evidence.size()));

        return view(application, evidence);
    }

    /**
     * 读取一次宽限申请（本人或馆员/管理员），返回完整证据列表。
     *
     * @param id 申请 id
     * @return 申请视图
     */
    @Transactional(readOnly = true)
    public GraceDtos.ApplicationView detail(long id) {
        GraceApplication application = require(id);
        LoginUser user = UserContext.require();
        if (!user.id().equals(application.getReaderId()) && !user.isStaff()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "宽限申请不存在");
        }
        return view(application, fileService.files(evidenceRepository.fileIdsOf(id)));
    }

    /**
     * 分页查询当前读者的宽限申请。
     *
     * @param status    状态过滤
     * @param pageQuery 分页参数
     * @return 申请分页
     */
    @Transactional(readOnly = true)
    public PageResult<GraceDtos.ApplicationView> myApplications(String status, PageQuery pageQuery) {
        Long readerId = UserContext.requireId();
        List<GraceApplication> applications =
                repository.findByReader(readerId, status, (int) pageQuery.offset(), pageQuery.size());
        long total = repository.countByReader(readerId, status);
        Map<Long, List<Long>> evidenceIds = evidenceRepository.fileIdsOf(
                applications.stream().map(GraceApplication::getId).toList());
        Map<Long, StoredFile> filesById = fileService
                .files(evidenceIds.values().stream().flatMap(List::stream).toList())
                .stream()
                .collect(Collectors.toMap(StoredFile::id, file -> file));
        List<GraceDtos.ApplicationView> items = applications.stream()
                .map(application -> view(
                        application,
                        evidenceIds.getOrDefault(application.getId(), List.of()).stream()
                                .map(filesById::get)
                                .filter(java.util.Objects::nonNull)
                                .toList()))
                .toList();
        return PageResult.of(items, total, pageQuery);
    }

    /**
     * 审批通过：写入实批天数并通知借阅上下文顺延应还日期。
     *
     * @param id      申请 ID
     * @param request 实批天数与备注
     * @return 审批视图
     */
    @Transactional
    public GraceDtos.GraceDecisionView approve(Long id, GraceDtos.ApproveRequest request) {
        Long operatorId = UserContext.requireId();
        LocalDateTime now = LocalDateTime.now();
        GraceApplication application = require(id);
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("status", application.getStatus().name());
        before.put("grantedDays", application.getGrantedDays() == null ? 0 : application.getGrantedDays());
        int days = request != null && request.days() != null
                ? request.days()
                : GraceCalculator.asDays(application.getComputedDays());
        application.approve(days, operatorId, now);
        if (repository.update(application) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "申请已被其他人处理，请刷新后重试");
        }

        BorrowOrder order = borrowService.applyGrace(application.getBorrowOrderId(), days);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("graceId", application.getId());
        payload.put("orderId", order.getId());
        payload.put("readerId", application.getReaderId());
        payload.put("days", days);
        payload.put("effectiveDueAt", String.valueOf(order.getEffectiveDueAt()));
        outboxAppender.append("GRACE_APPLICATION", application.getId(), MqConstant.EVENT_GRACE_APPROVED, payload);
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", application.getStatus().name());
        after.put("grantedDays", days);
        after.put("effectiveDueAt", String.valueOf(order.getEffectiveDueAt()));
        auditRecorder.record(
                "GRACE_APPROVE",
                "GRACE_APPLICATION",
                id,
                "宽限审批",
                Map.of("days", days, "note", request == null || request.note() == null ? "" : request.note()),
                before,
                after);
        return GraceDtos.GraceDecisionView.of(application);
    }

    /**
     * 驳回申请并记录原因码。
     *
     * @param id      申请 ID
     * @param request 驳回说明
     * @return 审批视图
     */
    @Transactional
    public GraceDtos.GraceDecisionView reject(Long id, GraceDtos.RejectRequest request) {
        Long operatorId = UserContext.requireId();
        GraceApplication application = require(id);
        Map<String, Object> before = Map.of("status", application.getStatus().name());
        String note = request == null || request.note() == null ? "" : request.note().trim();
        application.reject("REJECTED", operatorId, LocalDateTime.now());
        if (repository.update(application) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "申请已被其他人处理，请刷新后重试");
        }
        auditRecorder.record(
                "GRACE_REJECT",
                "GRACE_APPLICATION",
                id,
                "宽限审批",
                Map.of("note", note),
                before,
                Map.of("status", application.getStatus().name(), "rejectCode", "REJECTED"));
        return GraceDtos.GraceDecisionView.of(application);
    }

    private GraceDtos.ApplicationView view(GraceApplication application, List<StoredFile> evidence) {
        return new GraceDtos.ApplicationView(
                application.getId(),
                application.getApplyNo(),
                application.getBorrowOrderId(),
                application.getSceneCode(),
                application.getStatus().name(),
                application.getComputedDays() == null
                        ? null
                        : String.valueOf(GraceCalculator.asDays(application.getComputedDays())),
                application.getGrantedDays(),
                Times.iso(application.getSnapshotExpiresAt() == null
                        ? null
                        : application.getSnapshotExpiresAt()
                                .minusHours(ruleService.intValue("GRACE.SNAPSHOT_TTL_HOURS", 24))),
                Times.iso(application.getSnapshotExpiresAt()),
                evidence.stream().map(GraceDtos.EvidenceView::of).toList());
    }

    private GraceApplication require(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "宽限申请不存在"));
    }

    /**
     * 读取申请并校验归属，供申诉上下文使用。
     *
     * @param id       申请 ID
     * @param readerId 读者 ID
     * @return 宽限聚合
     */
    @Transactional(readOnly = true)
    public GraceApplication requireOwned(Long id, Long readerId) {
        GraceApplication application = require(id);
        if (!application.getReaderId().equals(readerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只能申诉自己的宽限申请");
        }
        return application;
    }

    private String nextApplyNo(LocalDateTime now) {
        int suffix = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 10000);
        return "GA" + now.format(APPLY_NO_DATE) + suffix;
    }

    /** 审批时用快照判断是否超时失效，存成标量 JSON 避免 LocalDateTime 序列化问题。 */
    private void cacheSnapshot(Long orderId, GraceApplication application, LocalDateTime expiresAt) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderId", orderId);
        snapshot.put("baseDays", application.getBaseDays().toPlainString());
        snapshot.put("kt", application.getKt().toPlainString());
        snapshot.put("kq", application.getKq().toPlainString());
        snapshot.put("computedDays", application.getComputedDays().toPlainString());
        snapshot.put("expiresAt", expiresAt.toString());
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            SNAPSHOT_PREFIX + orderId,
                            objectMapper.writeValueAsString(snapshot),
                            ruleService.intValue("GRACE.SNAPSHOT_TTL_HOURS", 24),
                            TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "宽限快照写入失败");
        }
    }
}
