package tw.huangcti.imrbs.infrastructure.messaging.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.infrastructure.integration.email.I18nEmailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * T128 [P] [US5] 會議提醒排程任務單元測試
 * 
 * 測試場景:
 * 1. 查詢即將開始的會議 (30 分鐘內)
 * 2. 發送提醒給預約者
 * 3. 跳過已發送提醒的預約 (避免重複發送)
 * 4. 處理無即將開始會議的情況
 * 5. 處理會議室資料不存在的情況
 * 6. 處理使用者資料不存在的情況
 * 7. Email 發送失敗不影響其他提醒
 * 8. 排程任務每 5 分鐘執行一次
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingReminderScheduler 單元測試")
class MeetingReminderSchedulerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private I18nEmailService emailService;

    @InjectMocks
    private MeetingReminderScheduler meetingReminderScheduler;

    private Reservation testReservation;
    private Room testRoom;
    private User testUser;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(25); // 25 分鐘後開始

        testReservation = Reservation.builder()
                .id(1L)
                .roomId(10L)
                .userId(100L)
                .meetingTitle("專案討論會議")
                .startTime(startTime)
                .endTime(startTime.plusHours(1))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .reminderSent(false)
                .build();

        testRoom = Room.builder()
                .id(10L)
                .name("A01 會議室")
                .building("總部大樓")
                .floor("3F")
                .capacity(10)
                .equipment(new ArrayList<>())
                .status(Room.RoomStatus.AVAILABLE)
                .build();

        testUser = User.builder()
                .id(100L)
                .employeeId("EMP001")
                .fullName("張三")
                .email("test@example.com")
                .languagePreference("zh-TW")
                .role(User.UserRole.EMPLOYEE)
                .build();
    }

    @Test
    @DisplayName("1. 查詢即將開始的會議 (30 分鐘內)")
    void testSendReminders_QueryUpcomingMeetings() {
        // Given
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Arrays.asList(testReservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        meetingReminderScheduler.sendReminders();

        // Then
        verify(reservationRepository, times(1)).findUpcomingReservationsForReminder(30);
    }

    @Test
    @DisplayName("2. 發送提醒給預約者")
    void testSendReminders_SendEmailToReserver() {
        // Given
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Arrays.asList(testReservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        meetingReminderScheduler.sendReminders();

        // Then
        verify(emailService, times(1)).sendMeetingReminder(
                eq(testReservation),
                eq("A01 會議室"),
                any(String.class),  // roomLocation
                any(String.class),  // roomEquipment
                eq(10),             // roomCapacity
                eq("張三"),
                eq("test@example.com"),
                any()
        );
    }

    @Test
    @DisplayName("3. 跳過已發送提醒的預約")
    void testSendReminders_SkipAlreadySentReminders() {
        // Given - 已發送提醒的預約不會在查詢結果中返回
        // Repository 直接返回空列表 (查詢已過濾 reminderSent=true 的預約)
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Collections.emptyList());

        // When
        meetingReminderScheduler.sendReminders();

        // Then - 不應發送 Email (因為沒有需要提醒的預約)
        verify(emailService, never()).sendMeetingReminder(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("4. 處理無即將開始會議的情況")
    void testSendReminders_NoUpcomingMeetings() {
        // Given
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Collections.emptyList());

        // When
        meetingReminderScheduler.sendReminders();

        // Then
        verify(reservationRepository, times(1)).findUpcomingReservationsForReminder(30);
        verify(emailService, never()).sendMeetingReminder(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("5. 處理會議室資料不存在的情況")
    void testSendReminders_RoomNotFound() {
        // Given
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Arrays.asList(testReservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        meetingReminderScheduler.sendReminders();

        // Then - 不應發送 Email
        verify(emailService, never()).sendMeetingReminder(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("6. 處理使用者資料不存在的情況")
    void testSendReminders_UserNotFound() {
        // Given
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Arrays.asList(testReservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        // When
        meetingReminderScheduler.sendReminders();

        // Then - 不應發送 Email
        verify(emailService, never()).sendMeetingReminder(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("7. Email 發送失敗不影響其他提醒")
    void testSendReminders_EmailFailureDoesNotAffectOthers() {
        // Given - 兩個預約
        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .roomId(10L)
                .userId(101L)
                .meetingTitle("第二個會議")
                .startTime(testReservation.getStartTime())
                .endTime(testReservation.getEndTime())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .reminderSent(false)
                .build();

        User user2 = User.builder()
                .id(101L)
                .employeeId("EMP002")
                .fullName("李四")
                .email("user2@example.com")
                .languagePreference("zh-TW")
                .role(User.UserRole.EMPLOYEE)
                .build();

        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Arrays.asList(testReservation, reservation2));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(101L)).thenReturn(Optional.of(user2));

        // 第一個發送失敗
        doThrow(new RuntimeException("SMTP failure"))
                .when(emailService).sendMeetingReminder(
                        eq(testReservation), any(), any(), any(), any(), any(), any(), any()
                );

        // When
        meetingReminderScheduler.sendReminders();

        // Then - 兩個都應嘗試發送
        verify(emailService, times(1)).sendMeetingReminder(
                eq(testReservation), any(), any(), any(), any(), any(), any(), any()
        );
        verify(emailService, times(1)).sendMeetingReminder(
                eq(reservation2), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("8. 發送提醒後更新 reminderSent 標記")
    void testSendReminders_UpdateReminderSentFlag() {
        // Given
        when(reservationRepository.findUpcomingReservationsForReminder(30))
                .thenReturn(Arrays.asList(testReservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        meetingReminderScheduler.sendReminders();

        // Then - 應更新 reminderSent 標記
        verify(reservationRepository, times(1)).save(argThat(reservation ->
                reservation.getId().equals(1L) && 
                Boolean.TRUE.equals(reservation.getReminderSent())
        ));
    }
}
