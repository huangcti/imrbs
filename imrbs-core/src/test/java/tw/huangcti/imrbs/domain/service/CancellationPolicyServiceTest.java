package tw.huangcti.imrbs.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CancellationPolicyServiceTest - 取消政策服務單元測試 (TDD)
 * 
 * 測試場景:
 * - 24 小時前取消預約應該成功
 * - 24 小時內取消預約應該失敗
 * - 邊界情況: 恰好 24 小時前
 * - 已取消的預約不能再次取消
 * - 已完成的預約不能取消
 */
@DisplayName("US2: 24 小時取消規則單元測試")
class CancellationPolicyServiceTest {
    
    private CancellationPolicyService cancellationPolicyService;
    
    @BeforeEach
    void setUp() {
        cancellationPolicyService = new CancellationPolicyService();
    }
    
    @Test
    @DisplayName("T078-1: 24 小時前取消預約應該成功")
    void testCanCancel_MoreThan24Hours_Success() {
        // Given - 預約時間為 25 小時後
        LocalDateTime now = LocalDateTime.of(2025, 11, 24, 10, 0);
        LocalDateTime meetingStart = now.plusHours(25);
        
        Reservation reservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L)
                .startTime(meetingStart)
                .endTime(meetingStart.plusHours(1))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        // When & Then - 應該可以取消
        assertDoesNotThrow(() -> 
            cancellationPolicyService.validateCancellation(reservation, now)
        );
    }
    
    @Test
    @DisplayName("T078-2: 24 小時內取消預約應該失敗")
    void testCanCancel_LessThan24Hours_Fail() {
        // Given - 預約時間為 23 小時後
        LocalDateTime now = LocalDateTime.of(2025, 11, 24, 10, 0);
        LocalDateTime meetingStart = now.plusHours(23);
        
        Reservation reservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L)
                .startTime(meetingStart)
                .endTime(meetingStart.plusHours(1))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        // When & Then - 應該拋出驗證異常
        ValidationException exception = assertThrows(ValidationException.class, () -> 
            cancellationPolicyService.validateCancellation(reservation, now)
        );
        
        assertTrue(exception.getMessage().contains("24 小時"));
    }
    
    @Test
    @DisplayName("T078-3: 恰好 24 小時前取消應該成功")
    void testCanCancel_Exactly24Hours_Success() {
        // Given - 預約時間為恰好 24 小時後
        LocalDateTime now = LocalDateTime.of(2025, 11, 24, 10, 0);
        LocalDateTime meetingStart = now.plusHours(24);
        
        Reservation reservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L)
                .startTime(meetingStart)
                .endTime(meetingStart.plusHours(1))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        // When & Then - 應該可以取消 (>=24 小時即可)
        assertDoesNotThrow(() -> 
            cancellationPolicyService.validateCancellation(reservation, now)
        );
    }
    
    @Test
    @DisplayName("T078-4: 已取消的預約不能再次取消")
    void testCanCancel_AlreadyCancelled_Fail() {
        // Given - 預約已經是取消狀態
        LocalDateTime now = LocalDateTime.of(2025, 11, 24, 10, 0);
        LocalDateTime meetingStart = now.plusHours(25);
        
        Reservation reservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L)
                .startTime(meetingStart)
                .endTime(meetingStart.plusHours(1))
                .status(Reservation.ReservationStatus.CANCELLED)
                .build();
        
        // When & Then - 應該拋出驗證異常
        ValidationException exception = assertThrows(ValidationException.class, () -> 
            cancellationPolicyService.validateCancellation(reservation, now)
        );
        
        assertTrue(exception.getMessage().contains("已取消") || 
                   exception.getMessage().contains("CANCELLED"));
    }
    
    @Test
    @DisplayName("T078-5: 已完成的預約不能取消")
    void testCanCancel_AlreadyCompleted_Fail() {
        // Given - 預約已經完成
        LocalDateTime now = LocalDateTime.of(2025, 11, 24, 10, 0);
        LocalDateTime meetingStart = now.minusHours(2);
        
        Reservation reservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L)
                .startTime(meetingStart)
                .endTime(meetingStart.plusHours(1))
                .status(Reservation.ReservationStatus.COMPLETED)
                .build();
        
        // When & Then - 應該拋出驗證異常
        ValidationException exception = assertThrows(ValidationException.class, () -> 
            cancellationPolicyService.validateCancellation(reservation, now)
        );
        
        assertTrue(exception.getMessage().contains("已完成") || 
                   exception.getMessage().contains("COMPLETED"));
    }
    
    @Test
    @DisplayName("T078-6: 會議已經開始不能取消")
    void testCanCancel_MeetingAlreadyStarted_Fail() {
        // Given - 會議已經開始
        LocalDateTime now = LocalDateTime.of(2025, 11, 24, 10, 0);
        LocalDateTime meetingStart = now.minusMinutes(10);
        
        Reservation reservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L)
                .startTime(meetingStart)
                .endTime(meetingStart.plusHours(1))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        // When & Then - 應該拋出驗證異常
        ValidationException exception = assertThrows(ValidationException.class, () -> 
            cancellationPolicyService.validateCancellation(reservation, now)
        );
        
        assertTrue(exception.getMessage().contains("24 小時") || 
                   exception.getMessage().contains("已開始"));
    }
    
    @Test
    @DisplayName("T078-7: 計算取消截止時間應該正確")
    void testCalculateCancellationDeadline() {
        // Given
        LocalDateTime meetingStart = LocalDateTime.of(2025, 11, 25, 14, 0);
        
        // When
        LocalDateTime deadline = cancellationPolicyService.calculateCancellationDeadline(meetingStart);
        
        // Then - 截止時間應該是會議開始前 24 小時
        LocalDateTime expected = LocalDateTime.of(2025, 11, 24, 14, 0);
        assertEquals(expected, deadline);
    }
    
    @Test
    @DisplayName("T078-8: 檢查是否在取消期限內應該正確")
    void testIsWithinCancellationPeriod() {
        // Given
        LocalDateTime meetingStart = LocalDateTime.of(2025, 11, 25, 14, 0);
        LocalDateTime now25HoursBefore = LocalDateTime.of(2025, 11, 24, 13, 0);
        LocalDateTime now23HoursBefore = LocalDateTime.of(2025, 11, 24, 15, 0);
        
        // When & Then
        assertTrue(cancellationPolicyService.isWithinCancellationPeriod(meetingStart, now25HoursBefore));
        assertFalse(cancellationPolicyService.isWithinCancellationPeriod(meetingStart, now23HoursBefore));
    }
}
