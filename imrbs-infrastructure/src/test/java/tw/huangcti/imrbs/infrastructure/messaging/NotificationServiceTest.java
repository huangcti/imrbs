package tw.huangcti.imrbs.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tw.huangcti.imrbs.domain.model.Notification;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.NotificationRepository;
import tw.huangcti.imrbs.infrastructure.config.RabbitMQConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceTest - 通知發送整合測試 (TDD)
 * 
 * 測試場景:
 * - 預約確認通知發送
 * - 預約取消通知發送
 * - Email 通知存儲
 * - RabbitMQ 訊息發送
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US1: 通知發送整合測試")
class NotificationServiceTest {
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private Reservation testReservation;
    
    @BeforeEach
    void setUp() {
        testReservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId("emp001")
                .startTime(LocalDateTime.of(2025, 11, 21, 9, 0))
                .endTime(LocalDateTime.of(2025, 11, 21, 10, 0))
                .meetingTitle("團隊週會")
                .participants("user1@example.com,user2@example.com")
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
    }
    
    @Test
    @DisplayName("T053-1: 應該成功發送預約確認通知")
    void testSendReservationConfirmation_Success() {
        // Given
        String recipientEmail = "user@example.com";
        Notification savedNotification = Notification.builder()
                .id(1L)
                .reservationId(testReservation.getId())
                .recipientEmail(recipientEmail)
                .notificationType(Notification.NotificationType.RESERVATION_CONFIRMED)
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(savedNotification);
        
        // When
        notificationService.sendReservationConfirmation(testReservation, recipientEmail);
        
        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any()
        );
    }
    
    @Test
    @DisplayName("T053-2: 通知應該包含正確的資訊")
    void testSendReservationConfirmation_CorrectContent() {
        // Given
        String recipientEmail = "user@example.com";
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Notification.builder().id(1L).build());
        
        // When
        notificationService.sendReservationConfirmation(testReservation, recipientEmail);
        
        // Then
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification capturedNotification = notificationCaptor.getValue();
        
        assertEquals(testReservation.getId(), capturedNotification.getReservationId());
        assertEquals(recipientEmail, capturedNotification.getRecipientEmail());
        assertEquals(Notification.NotificationType.RESERVATION_CONFIRMED, capturedNotification.getNotificationType());
        assertNotNull(capturedNotification.getSubject());
        assertNotNull(capturedNotification.getBody());
        assertTrue(capturedNotification.getSubject().contains(testReservation.getMeetingTitle()));
    }
    
    @Test
    @DisplayName("T053-3: 應該成功發送預約取消通知")
    void testSendReservationCancellation_Success() {
        // Given
        String recipientEmail = "user@example.com";
        String cancellationReason = "會議取消";
        
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Notification.builder().id(1L).build());
        
        // When
        notificationService.sendReservationCancellation(testReservation, recipientEmail, cancellationReason);
        
        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any()
        );
    }
    
    @Test
    @DisplayName("T053-4: 取消通知應該包含取消原因")
    void testSendReservationCancellation_IncludesReason() {
        // Given
        String recipientEmail = "user@example.com";
        String cancellationReason = "會議取消";
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Notification.builder().id(1L).build());
        
        // When
        notificationService.sendReservationCancellation(testReservation, recipientEmail, cancellationReason);
        
        // Then
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification capturedNotification = notificationCaptor.getValue();
        
        assertTrue(capturedNotification.getBody().contains(cancellationReason));
        assertEquals(Notification.NotificationType.RESERVATION_CANCELLED, capturedNotification.getNotificationType());
    }
    
    @Test
    @DisplayName("T053-5: 發送失敗應該記錄錯誤")
    void testSendNotification_Failure() {
        // Given
        String recipientEmail = "user@example.com";
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Notification.builder().id(1L).build());
        
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any());
        
        // When & Then
        assertThrows(RuntimeException.class, () ->
                notificationService.sendReservationConfirmation(testReservation, recipientEmail));
    }
    
    @Test
    @DisplayName("T053-6: 應該支援批量發送通知給參與者")
    void testSendToMultipleParticipants_Success() {
        // Given
        String participants = "user1@example.com,user2@example.com,user3@example.com";
        testReservation = testReservation.toBuilder()
                .participants(participants)
                .build();
        
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Notification.builder().id(1L).build());
        
        // When
        notificationService.sendReservationConfirmationToParticipants(testReservation);
        
        // Then
        verify(notificationRepository, times(3)).save(any(Notification.class));
        verify(rabbitTemplate, times(3)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any()
        );
    }
    
    @Test
    @DisplayName("T053-7: 30 分鐘提醒應該使用 Reminder Queue")
    void testSendMeetingReminder_UsesReminderQueue() {
        // Given
        String recipientEmail = "user@example.com";
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Notification.builder().id(1L).build());
        
        // When
        notificationService.scheduleMeetingReminder(testReservation, recipientEmail);
        
        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.REMINDER_ROUTING_KEY),
                any()
        );
    }
}
