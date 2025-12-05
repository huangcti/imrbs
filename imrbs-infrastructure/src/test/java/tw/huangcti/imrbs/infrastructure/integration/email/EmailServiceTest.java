package tw.huangcti.imrbs.infrastructure.integration.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * T127 [P] [US5] Email 通知發送單元測試
 * 
 * 測試場景:
 * 1. 發送預約確認通知 - 成功
 * 2. 發送預約確認通知 - SMTP 失敗拋出異常
 * 3. 發送預約取消通知 - 成功
 * 4. 發送預約取消通知 - 包含取消原因
 * 5. 發送會議提醒 - 成功
 * 6. 發送會議提醒 - 失敗不拋出異常 (避免影響主流程)
 * 7. Email 內容包含必要資訊 (會議主題、時間、地點)
 * 8. Email 主旨格式正確
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 單元測試")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private Reservation testReservation;
    private final String testRoomName = "A01 會議室";
    private final String testUserName = "張三";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // 設定 fromEmail 屬性
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@imrbs.example.com");

        // 建立測試預約
        testReservation = Reservation.builder()
                .id(1L)
                .roomId(10L)
                .userId(100L)
                .meetingTitle("專案討論會議")
                .startTime(LocalDateTime.of(2025, 12, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 12, 1, 15, 0))
                .description("討論 Q4 目標")
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("1. 發送預約確認通知 - 成功")
    void testSendReservationConfirmation_Success() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendReservationConfirmation(
                testReservation, testRoomName, testUserName, testEmail
        );

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(testEmail);
        assertThat(sentMessage.getFrom()).isEqualTo("noreply@imrbs.example.com");
        assertThat(sentMessage.getSubject()).contains("會議室預約確認");
        assertThat(sentMessage.getSubject()).contains("專案討論會議");
        assertThat(sentMessage.getText()).contains(testUserName);
        assertThat(sentMessage.getText()).contains(testRoomName);
        assertThat(sentMessage.getText()).contains("2025-12-01 14:00");
    }

    @Test
    @DisplayName("2. 發送預約確認通知 - SMTP 失敗拋出異常")
    void testSendReservationConfirmation_SmtpFailure() {
        // Given
        doThrow(new RuntimeException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> 
            emailService.sendReservationConfirmation(
                    testReservation, testRoomName, testUserName, testEmail
            )
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Email 發送失敗");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("3. 發送預約取消通知 - 成功")
    void testSendReservationCancellation_Success() {
        // Given
        String cancellationReason = "會議取消";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendReservationCancellation(
                testReservation, testRoomName, testUserName, testEmail, cancellationReason
        );

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(testEmail);
        assertThat(sentMessage.getSubject()).contains("會議室預約取消");
        assertThat(sentMessage.getSubject()).contains("專案討論會議");
    }

    @Test
    @DisplayName("4. 發送預約取消通知 - 包含取消原因")
    void testSendReservationCancellation_WithReason() {
        // Given
        String cancellationReason = "場地設備故障";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendReservationCancellation(
                testReservation, testRoomName, testUserName, testEmail, cancellationReason
        );

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).contains(cancellationReason);
        assertThat(sentMessage.getText()).contains("場地設備故障");
    }

    @Test
    @DisplayName("5. 發送會議提醒 - 成功")
    void testSendMeetingReminder_Success() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMeetingReminder(
                testReservation, testRoomName, testUserName, testEmail
        );

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(testEmail);
        assertThat(sentMessage.getSubject()).contains("會議提醒");
        assertThat(sentMessage.getSubject()).contains("專案討論會議");
        assertThat(sentMessage.getText()).contains("分鐘後開始");
    }

    @Test
    @DisplayName("6. 發送會議提醒 - 失敗不拋出異常")
    void testSendMeetingReminder_FailureDoesNotThrow() {
        // Given
        doThrow(new RuntimeException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then - 不應拋出異常
        emailService.sendMeetingReminder(
                testReservation, testRoomName, testUserName, testEmail
        );

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("7. Email 內容包含必要資訊")
    void testEmailContent_ContainsRequiredInfo() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendReservationConfirmation(
                testReservation, testRoomName, testUserName, testEmail
        );

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String emailBody = sentMessage.getText();

        // 驗證必要資訊
        assertThat(emailBody).contains("專案討論會議"); // 會議主題
        assertThat(emailBody).contains("A01 會議室"); // 會議室
        assertThat(emailBody).contains("2025-12-01"); // 日期
        assertThat(emailBody).contains("14:00"); // 開始時間
        assertThat(emailBody).contains("15:00"); // 結束時間
        assertThat(emailBody).contains("張三"); // 預約者
    }

    @Test
    @DisplayName("8. Email 主旨格式正確")
    void testEmailSubject_CorrectFormat() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When - 測試確認通知
        emailService.sendReservationConfirmation(
                testReservation, testRoomName, testUserName, testEmail
        );

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getSubject())
                .isEqualTo("會議室預約確認 - 專案討論會議");

        // When - 測試取消通知
        reset(mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendReservationCancellation(
                testReservation, testRoomName, testUserName, testEmail, "測試取消"
        );

        // Then
        messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSubject())
                .isEqualTo("會議室預約取消 - 專案討論會議");
    }
}
