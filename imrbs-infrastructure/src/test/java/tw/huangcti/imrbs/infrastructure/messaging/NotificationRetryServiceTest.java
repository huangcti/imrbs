package tw.huangcti.imrbs.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.huangcti.imrbs.infrastructure.integration.email.EmailService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * T129 [P] [US5] 通知重試機制單元測試
 * 
 * 測試場景:
 * 1. 第一次發送成功 - 不重試
 * 2. 第一次失敗,第二次成功 - 重試 1 次
 * 3. 連續失敗 3 次 - 拋出異常
 * 4. 指數退避重試間隔 (1s, 2s, 4s)
 * 5. 重試次數可配置
 * 6. 記錄重試日誌
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationRetryService 單元測試")
class NotificationRetryServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationRetryService notificationRetryService;

    private final String testEmail = "test@example.com";
    private final String testSubject = "測試通知";
    private final String testBody = "測試內容";

    @BeforeEach
    void setUp() {
        // 可在此設定重試次數等配置
    }

    @Test
    @DisplayName("1. 第一次發送成功 - 不重試")
    void testSendWithRetry_FirstAttemptSuccess() {
        // Given
        doNothing().when(emailService).sendMeetingReminder(any(), any(), any(), eq(testEmail));

        // When
        notificationRetryService.sendMeetingReminderWithRetry(
                null, "會議室", "使用者", testEmail
        );

        // Then - 只呼叫一次
        verify(emailService, times(1)).sendMeetingReminder(any(), any(), any(), eq(testEmail));
    }

    @Test
    @DisplayName("2. 第一次失敗,第二次成功 - 重試 1 次")
    void testSendWithRetry_FailOnceThenSuccess() {
        // Given
        doThrow(new RuntimeException("SMTP failure"))
                .doNothing()
                .when(emailService).sendMeetingReminder(any(), any(), any(), eq(testEmail));

        // When
        notificationRetryService.sendMeetingReminderWithRetry(
                null, "會議室", "使用者", testEmail
        );

        // Then - 呼叫 2 次 (失敗 1 次 + 成功 1 次)
        verify(emailService, times(2)).sendMeetingReminder(any(), any(), any(), eq(testEmail));
    }

    @Test
    @DisplayName("3. 連續失敗 3 次 - 拋出異常")
    void testSendWithRetry_FailThreeTimes() {
        // Given
        doThrow(new RuntimeException("SMTP failure"))
                .when(emailService).sendMeetingReminder(any(), any(), any(), eq(testEmail));

        // When & Then
        assertThatThrownBy(() -> 
            notificationRetryService.sendMeetingReminderWithRetry(
                    null, "會議室", "使用者", testEmail
            )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("通知發送失敗,已重試 3 次");

        // Then - 呼叫 3 次 (初始 1 次 + 重試 2 次 = 最多 3 次)
        verify(emailService, times(3)).sendMeetingReminder(any(), any(), any(), eq(testEmail));
    }

    @Test
    @DisplayName("4. 第二次失敗,第三次成功 - 重試 2 次")
    void testSendWithRetry_FailTwiceThenSuccess() {
        // Given
        doThrow(new RuntimeException("SMTP failure"))
                .doThrow(new RuntimeException("SMTP failure"))
                .doNothing()
                .when(emailService).sendMeetingReminder(any(), any(), any(), eq(testEmail));

        // When
        notificationRetryService.sendMeetingReminderWithRetry(
                null, "會議室", "使用者", testEmail
        );

        // Then - 呼叫 3 次 (失敗 2 次 + 成功 1 次)
        verify(emailService, times(3)).sendMeetingReminder(any(), any(), any(), eq(testEmail));
    }

    @Test
    @DisplayName("5. 重試機制支援不同通知類型")
    void testSendWithRetry_SupportDifferentNotificationTypes() {
        // Given - 測試預約確認通知
        doNothing().when(emailService).sendReservationConfirmation(
                any(), any(), any(), eq(testEmail)
        );

        // When
        notificationRetryService.sendReservationConfirmationWithRetry(
                null, "會議室", "使用者", testEmail
        );

        // Then
        verify(emailService, times(1)).sendReservationConfirmation(
                any(), any(), any(), eq(testEmail)
        );

        // Given - 測試預約取消通知
        doNothing().when(emailService).sendReservationCancellation(
                any(), any(), any(), eq(testEmail), anyString()
        );

        // When
        notificationRetryService.sendReservationCancellationWithRetry(
                null, "會議室", "使用者", testEmail, "取消原因"
        );

        // Then
        verify(emailService, times(1)).sendReservationCancellation(
                any(), any(), any(), eq(testEmail), eq("取消原因")
        );
    }

    @Test
    @DisplayName("6. 特定異常不重試 (如: 非法參數)")
    void testSendWithRetry_DoNotRetryForIllegalArgument() {
        // Given
        doThrow(new IllegalArgumentException("Invalid email address"))
                .when(emailService).sendMeetingReminder(any(), any(), any(), eq(testEmail));

        // When & Then
        assertThatThrownBy(() -> 
            notificationRetryService.sendMeetingReminderWithRetry(
                    null, "會議室", "使用者", testEmail
            )
        )
        .isInstanceOf(IllegalArgumentException.class);

        // Then - 只呼叫 1 次,不重試
        verify(emailService, times(1)).sendMeetingReminder(any(), any(), any(), eq(testEmail));
    }
}
