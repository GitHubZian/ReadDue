package com.zian.backend.notify.application;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.application.BorrowQueryService;
import com.zian.backend.notify.domain.model.NotifyTask;
import com.zian.backend.notify.domain.model.NotifyTaskStatus;
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

    // ---------------------------------------------------------------- 计划

    /**
     * 借出后计划到期提醒：L1（到期前 3 天）、L2（到期当天）。
     */
    @Transactional
    public void planReturnReminders(Long orderId, Long readerId, LocalDate dueAt) {
        LocalDateTime l1 = dueAt.minusDays(3).atTime(LocalTime.of(9, 0));
        LocalDateTime l2 = dueAt.atTime(LocalTime.of(9, 0));
        plan("RETURN_REMIND:" + orderId + ":L1", readerId, orderId, LEVEL_L1, l1, payload(orderId, null));
        plan("RETURN_REMIND:" + orderId + ":L2", readerId, orderId, LEVEL_L2, l2, payload(orderId, null));
    }

    /**
     * 逾期标记后立即计划 L3 提醒。
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
     * 预约到书立即通知（P0 直投，不进入静默窗口）。
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
     * 归还后取消未发出的提醒。
     */
    @Transactional
    public int cancelReminders(Long orderId) {
        return taskRepository.cancelPlannedByOrder(orderId);
    }

    /**
     * 宽限通过后重排提醒：取消原 L2/L3，按新的应还日重新计划 L1/L2。
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

    // ---------------------------------------------------------------- 投递

    /**
     * 到点投递：站内信落库。
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
     * 手动重试失败任务。
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
     * 管理端批量催还：为仍在借的订单立即生成 L3 提醒并投递。
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
    }

    // ---------------------------------------------------------------- 静默窗口

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
}
