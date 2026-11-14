package com.zian.backend.notify.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.application.BorrowQueryService;
import com.zian.backend.notify.domain.model.NotifyTask;
import com.zian.backend.notify.domain.model.NotifyTaskStatus;
import com.zian.backend.notify.domain.port.EmailNotifier;
import com.zian.backend.notify.domain.repository.ChannelPrefRepository;
import com.zian.backend.notify.domain.repository.NotifyDeliveryRepository;
import com.zian.backend.notify.domain.repository.NotifyTaskRepository;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知编排：计划 / 取消 / 到点投递 / 手动重试 / 批量催还。
 *
 * <p>站内信落库即视为投递成功；邮件由 q.delivery.email 异步消费者处理（本地无 SMTP 时降级为站内信）。
 */
@Service
@RequiredArgsConstructor
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);

    public static final String LEVEL_L1 = "L1";
    public static final String LEVEL_L2 = "L2";
    public static final String LEVEL_L3 = "L3";
    public static final String LEVEL_GRACE = "GRACE_RESULT";
    public static final String LEVEL_APPEAL = "APPEAL_SLA";
    public static final String LEVEL_READY = "RESERVATION_READY";
    public static final String LEVEL_SYSTEM = "SYSTEM";

    private final NotifyTaskRepository taskRepository;
    private final NotifyDeliveryRepository deliveryRepository;
    private final BorrowQueryService borrowQueryService;
    private final RuleService ruleService;
    private final AuditRecorder auditRecorder;
    private final ChannelPrefRepository channelPrefRepository;
    private final EmailNotifier emailNotifier;

// region 计划


    /**
     * 规划到期提醒：生成 L1（到期前 3 天）与 L2（到期当天）两条待发任务。
     *
     * @param orderId  借阅单 ID
     * @param readerId 读者 ID
     * @param dueAt    有效应还日
     */
    @Transactional
    public void planReturnReminders(Long orderId, Long readerId, LocalDate dueAt) {
        LocalDateTime l1 = dueAt.minusDays(3).atTime(LocalTime.of(9, 0));
        LocalDateTime l2 = dueAt.atTime(LocalTime.of(9, 0));
        plan("RETURN_REMIND:" + orderId + ":L1", readerId, orderId, LEVEL_L1, l1, payload(orderId, null));
        plan("RETURN_REMIND:" + orderId + ":L2", readerId, orderId, LEVEL_L2, l2, payload(orderId, null));
    }

    /**
     * 规划逾期提醒：生成当日 L3 任务（按借阅单与日期去重）。
     *
     * @param orderId     借阅单 ID
     * @param readerId    读者 ID
     * @param overdueDays 逾期天数
     */
    @Transactional
    public void planOverdue(Long orderId, Long readerId, int overdueDays) {
        plan(
                "OVERDUE:" + orderId + ":" + LocalDate.now(),
                readerId,
                orderId,
                LEVEL_L3,
                LocalDateTime.now(),
                payload(orderId, overdueDays));
    }

    /**
     * 到书通知：立即投递并绕过静默窗口。
     *
     * @param reservationId 预约 ID
     * @param readerId      读者 ID
     * @param bookId        书目 ID
     * @param expiresAt     取书截止时间
     */
    @Transactional
    public void planReservationReady(Long reservationId, Long readerId, Long bookId, LocalDateTime expiresAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reservationId", reservationId);
        payload.put("bookId", bookId);
        payload.put("expiresAt", expiresAt == null ? null : expiresAt.toString());
        Long id = plan(
                "RESERVATION_READY:" + reservationId,
                readerId,
                null,
                LEVEL_READY,
                LocalDateTime.now(),
                payload,
                true);
        if (id != null) {
            taskRepository.findById(id).ifPresent(this::dispatch);
        }
    }

    /**
     * 取消该借阅单尚未发送的提醒。
     *
     * @param orderId 借阅单 ID
     * @return 取消条数
     */
    @Transactional
    public int cancelReminders(Long orderId) {
        return taskRepository.cancelPlannedByOrder(orderId);
    }

    /**
     * 宽限通过后重排提醒：取消旧的 L2/L3，按有效应还日重建 L2。
     *
     * @param orderId        借阅单 ID
     * @param readerId       读者 ID
     * @param effectiveDueAt 新的有效应还日
     */
    @Transactional
    public void replanAfterGrace(Long orderId, Long readerId, LocalDate effectiveDueAt) {
        taskRepository.cancelPlannedByOrderAndLevels(orderId, List.of(LEVEL_L2, LEVEL_L3));
        plan(
                "RETURN_REMIND:" + orderId + ":L2:" + effectiveDueAt,
                readerId,
                orderId,
                LEVEL_L2,
                effectiveDueAt.atTime(LocalTime.of(9, 0)),
                payload(orderId, null));
    }

    /**
     * 续借后重排提醒：取消 L1–L3，按新应还日重建 L1/L2。
     *
     * @param orderId        借阅单 ID
     * @param readerId       读者 ID
     * @param effectiveDueAt 新的有效应还日
     */
    @Transactional
    public void replanAfterRenewal(Long orderId, Long readerId, LocalDate effectiveDueAt) {
        taskRepository.cancelPlannedByOrderAndLevels(orderId, List.of(LEVEL_L1, LEVEL_L2, LEVEL_L3));
        plan(
                "RETURN_REMIND:" + orderId + ":L1:" + effectiveDueAt,
                readerId,
                orderId,
                LEVEL_L1,
                effectiveDueAt.minusDays(3).atTime(LocalTime.of(9, 0)),
                payload(orderId, null));
        plan(
                "RETURN_REMIND:" + orderId + ":L2:" + effectiveDueAt,
                readerId,
                orderId,
                LEVEL_L2,
                effectiveDueAt.atTime(LocalTime.of(9, 0)),
                payload(orderId, null));
    }

    private Long plan(
            String dedupKey,
            Long readerId,
            Long orderId,
            String level,
            LocalDateTime planAt,
            Map<String, Object> payload) {
        return plan(dedupKey, readerId, orderId, level, planAt, payload, false);
    }

    private Long plan(
            String dedupKey,
            Long readerId,
            Long orderId,
            String level,
            LocalDateTime planAt,
            Map<String, Object> payload,
            boolean bypassQuiet) {
        if (taskRepository.findByDedupKey(dedupKey).isPresent()) {
            return null;
        }
        LocalDateTime shifted = bypassQuiet ? planAt : shiftOutOfQuiet(planAt);
        boolean quietShifted = !shifted.equals(planAt);
        NotifyTask task = new NotifyTask(
                null,
                dedupKey,
                readerId,
                orderId,
                level,
                shifted,
                quietShifted,
                NotifyTaskStatus.PLANNED,
                payload,
                null);
        return taskRepository.insert(task);
    }

    private Map<String, Object> payload(Long orderId, Integer overdueDays) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId);
        if (overdueDays != null) {
            payload.put("overdueDays", overdueDays);
        }
        return payload;
    }

// endregion

// region 投递


    /**
     * 定时投递到期任务：写入站内信并按需投递邮件。
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    @Transactional
    public void dispatchDue() {
        List<NotifyTask> due;
        try {
            due = taskRepository.findDue(LocalDateTime.now(), 50);
        } catch (Exception e) {
            log.warn("通知派发扫描失败: {}", e.getMessage());
            return;
        }
        for (NotifyTask task : due) {
            dispatch(task);
        }
    }

    /**
     * 重试失败/部分成功的通知任务。
     *
     * @param id 任务 ID
     */
    @Transactional
    public void retry(Long id) {
        NotifyTask task = taskRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "通知任务不存在"));
        if (task.status() != NotifyTaskStatus.FAILED && task.status() != NotifyTaskStatus.PARTIAL) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "只有失败的推送任务可以重试");
        }
        dispatch(task);
        auditRecorder.record("NOTIFY_RETRY", "NOTIFY_TASK", id, "通知重试", Map.of("level", task.level()));
    }

    /**
     * 批量催还：对指定借阅单立即生成并投递 L3 提醒。
     *
     * @param orderIds 借阅单 ID 列表，单次上限 500
     * @return 实际投递条数
     */
    @Transactional
    public int remindBatch(List<Long> orderIds) {
        List<Long> ids = orderIds == null ? List.of() : orderIds;
        if (ids.size() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "单次最多催还 500 条");
        }
        int sent = 0;
        List<Long> touched = new ArrayList<>();
        for (BorrowQueryService.ActiveBorrow borrow : borrowQueryService.findActive(ids)) {
            int overdueDays = (int) Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(
                    borrow.effectiveDueAt(), LocalDate.now()));
            Long taskId = plan(
                    "OVERDUE:" + borrow.id() + ":" + LocalDate.now(),
                    borrow.readerId(),
                    borrow.id(),
                    LEVEL_L3,
                    LocalDateTime.now(),
                    payload(borrow.id(), overdueDays));
            if (taskId != null) {
                taskRepository.findById(taskId).ifPresent(this::dispatch);
                sent++;
                touched.add(borrow.id());
            }
        }
        if (!touched.isEmpty()) {
            auditRecorder.record(
                    "NOTIFY_REMIND_BATCH", "BORROW_ORDER", touched.get(0), "批量催还", Map.of("count", sent));
        }
        return sent;
    }

    private void dispatch(NotifyTask task) {
        NotifyTask sent = new NotifyTask(
                task.id(),
                task.dedupKey(),
                task.readerId(),
                task.borrowOrderId(),
                task.level(),
                task.planAt(),
                task.quietShifted(),
                NotifyTaskStatus.SENT,
                task.payload(),
                task.createdAt());
        if (taskRepository.update(sent) == 0) {
            return;
        }
        deliveryRepository.append(task.id(), "INBOX", "SENT", 1, null, null);
        if (channelPrefRepository.emailEnabled(task.readerId())) {
            emailNotifier.enqueue(task.id(), task.readerId(), subjectOf(task.level()), bodyOf(task));
        }
    }

    private static String subjectOf(String level) {
        return switch (level) {
            case LEVEL_L1 -> "图书到期前提醒";
            case LEVEL_L2 -> "图书今日到期";
            case LEVEL_L3 -> "图书逾期提醒";
            case LEVEL_GRACE -> "宽限申请结果";
            case LEVEL_READY -> "预约到书通知";
            case LEVEL_APPEAL -> "申诉受理通知";
            default -> "阅有期通知";
        };
    }

    private static String bodyOf(NotifyTask task) {
        String subject = subjectOf(task.level());
        String order = task.borrowOrderId() == null ? "" : "借阅单：" + task.borrowOrderId() + "。";
        return "读者您好，" + subject + "。" + order + "请登录阅有期查看详情。";
    }

// endregion

// region 静默窗口


    /**
     * 22:00-08:00 的计划时间平移到窗口结束时刻。
     */
    private LocalDateTime shiftOutOfQuiet(LocalDateTime planAt) {
        LocalTime start = LocalTime.parse(ruleService.text("QUIET.START", "22:00"));
        LocalTime end = LocalTime.parse(ruleService.text("QUIET.END", "08:00"));
        LocalTime time = planAt.toLocalTime();
        boolean inWindow = start.isAfter(end)
                ? (time.isAfter(start) || time.isBefore(end) || time.equals(start))
                : (time.isAfter(start) && time.isBefore(end));
        if (!inWindow) {
            return planAt;
        }
        LocalDate date = time.isBefore(end) || time.equals(end) ? planAt.toLocalDate() : planAt.toLocalDate().plusDays(1);
        return date.atTime(end);
    }

// endregion

}
