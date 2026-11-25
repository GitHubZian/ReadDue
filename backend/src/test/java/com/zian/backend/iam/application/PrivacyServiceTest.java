package com.zian.backend.iam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zian.backend.audit.application.AuditRecorder;
import com.zian.backend.borrow.domain.repository.BorrowOrderRepository;
import com.zian.backend.iam.domain.model.PrivacyState;
import com.zian.backend.iam.domain.model.User;
import com.zian.backend.iam.domain.repository.PrivacyRepository;
import com.zian.backend.iam.domain.repository.UserRepository;
import com.zian.backend.iam.interfaces.dto.PrivacyDtos;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * task-8：遗忘成功后清理该读者借阅单的宽限计算快照，且只删这些键。
 */
@ExtendWith(MockitoExtension.class)
class PrivacyServiceTest {

    private static final long USER_ID = 42L;

    @Mock
    private PrivacyRepository privacyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditRecorder auditRecorder;
    @Mock
    private BorrowOrderRepository borrowOrderRepository;
    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private PrivacyService privacyService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    @DisplayName("遗忘后只删除该读者借阅单对应的 grace:snap 键")
    void forgetDeletesReaderGraceSnapshots() {
        loginAsReader();
        when(privacyRepository.find(USER_ID)).thenReturn(Optional.of(PrivacyState.minimal()));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(borrowOrderRepository.findIdsByReader(USER_ID)).thenReturn(List.of(11L, 22L));

        PrivacyDtos.ForgetResult result = privacyService.forget();

        assertThat(result.forgotten()).isTrue();
        assertThat(result.displayName()).isEqualTo("已注销读者-1001");
        verify(privacyRepository).anonymize(eq(USER_ID), startsWith("已注销读者-"), any());
        verify(redisTemplate).delete(List.of("readdue:grace:snap:11", "readdue:grace:snap:22"));
        verify(auditRecorder)
                .record(eq("USER_FORGET"), eq("SYS_USER"), eq(USER_ID), anyString(), anyMap(), anyMap(), anyMap());
    }

    @Test
    @DisplayName("Redis 不可用不影响脱敏结果")
    void redisFailureDoesNotBreakForget() {
        loginAsReader();
        when(privacyRepository.find(USER_ID)).thenReturn(Optional.of(PrivacyState.minimal()));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(borrowOrderRepository.findIdsByReader(USER_ID)).thenReturn(List.of(11L));
        doThrow(new IllegalStateException("redis down")).when(redisTemplate).delete(anyCollection());

        PrivacyDtos.ForgetResult result = privacyService.forget();

        assertThat(result.forgotten()).isTrue();
        verify(privacyRepository).anonymize(eq(USER_ID), startsWith("已注销读者-"), any());
    }

    private static void loginAsReader() {
        UserContext.set(new LoginUser(USER_ID, "2021001", "陈昊", "READER", Set.of()));
    }

    private static User user() {
        return User.builder().id(USER_ID).userNo("2021001").name("陈昊").build();
    }
}
