package com.zian.backend.notify.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.application.BorrowQueryService;
import com.zian.backend.notify.domain.model.ChannelPreferences;
import com.zian.backend.notify.domain.model.NotifyTask;
import com.zian.backend.notify.domain.model.NotifyTaskStatus;
import com.zian.backend.notify.domain.model.NotifyTopic;
import com.zian.backend.notify.domain.port.EmailNotifier;
import com.zian.backend.notify.domain.repository.ChannelPrefRepository;
import com.zian.backend.notify.domain.repository.NotifyDeliveryRepository;
import com.zian.backend.notify.domain.repository.NotifyPreferenceRepository;
import com.zian.backend.notify.domain.repository.NotifyTaskRepository;
import com.zian.backend.notify.domain.repository.NotifyTopicRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * task-8：P0 必达主题不能被全局渠道开关关掉站内信。
 */
@ExtendWith(MockitoExtension.class)
class NotifyServiceDispatchTest {

    private static final long TASK_ID = 90001L;
    private static final long READER_ID = 7L;
    private static final long ORDER_ID = 55L;

    @Mock
    private NotifyTaskRepository taskRepository;
    @Mock
    private NotifyDeliveryRepository deliveryRepository;
    @Mock
    private BorrowQueryService borrowQueryService;
    @Mock
    private AuditRecorder auditRecorder;
    @Mock
    private ChannelPrefRepository channelPrefRepository;
    @Mock
    private QuietHoursService quietHoursService;
    @Mock
    private EmailNotifier emailNotifier;
    @Mock
    private NotifyTopicRepository topicRepository;
    @Mock
    private NotifyPreferenceRepository preferenceRepository;

    @InjectMocks
    private NotifyService notifyService;

    @Test
    @DisplayName("P0 主题关闭站内信仍强制落 INBOX")
    void p0TopicForcesInboxEvenWhenChannelOff() {
        NotifyTask task = task("GRACE_RESULT");
        when(taskRepository.findDue(any(LocalDateTime.class), eq(50))).thenReturn(List.of(task));
        when(taskRepository.update(any(NotifyTask.class))).thenReturn(1);
        when(channelPrefRepository.load(READER_ID)).thenReturn(new ChannelPreferences(false, false));
        when(topicRepository.findByCode("GRACE_RESULT")).thenReturn(Optional.of(topic("GRACE_RESULT", "P0")));

        notifyService.dispatchDue();

        verify(deliveryRepository).append(TASK_ID, "INBOX", "SENT", 1, null, null);
        verify(emailNotifier, never()).enqueue(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("非 P0 主题尊重站内信开关")
    void normalTopicRespectsInboxSwitch() {
        NotifyTask task = task("BORROW_DUE");
        when(taskRepository.findDue(any(LocalDateTime.class), eq(50))).thenReturn(List.of(task));
        when(taskRepository.update(any(NotifyTask.class))).thenReturn(1);
        when(channelPrefRepository.load(READER_ID)).thenReturn(new ChannelPreferences(false, false));
        when(topicRepository.findByCode("BORROW_DUE")).thenReturn(Optional.of(topic("BORROW_DUE", "P1")));

        notifyService.dispatchDue();

        verify(deliveryRepository, never())
                .append(anyLong(), anyString(), anyString(), anyInt(), any(), any());
    }

    private static NotifyTask task(String topicCode) {
        return new NotifyTask(
                TASK_ID,
                "DEDUP-" + TASK_ID,
                READER_ID,
                ORDER_ID,
                "L2",
                LocalDateTime.now(),
                false,
                NotifyTaskStatus.PLANNED,
                Map.of("orderId", ORDER_ID),
                topicCode,
                Map.of("channels", List.of("INBOX")),
                null);
    }

    private static NotifyTopic topic(String code, String priority) {
        return new NotifyTopic(1L, code, code, priority, "REALTIME", List.of("INBOX"), true, 1);
    }
}
