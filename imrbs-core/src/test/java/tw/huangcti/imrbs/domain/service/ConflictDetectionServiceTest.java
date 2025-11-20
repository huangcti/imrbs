package tw.huangcti.imrbs.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ConflictDetectionServiceTest - 衝突檢測服務單元測試 (TDD)
 * 
 * 測試場景:
 * - 預約時段與現有預約衝突
 * - 預約時段與維護時段衝突
 * - 無衝突情況
 * - 邊界條件測試 (緊鄰時段)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US1: 預約衝突檢測單元測試")
class ConflictDetectionServiceTest {
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private MaintenanceScheduleRepository maintenanceScheduleRepository;
    
    @InjectMocks
    private ConflictDetectionService conflictDetectionService;
    
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @BeforeEach
    void setUp() {
        roomId = 1L;
        startTime = LocalDateTime.of(2025, 11, 21, 9, 0);
        endTime = LocalDateTime.of(2025, 11, 21, 10, 0);
    }
    
    @Test
    @DisplayName("T052-1: 無衝突時應該通過檢查")
    void testCheckConflict_NoConflict() {
        // Given
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        assertDoesNotThrow(() -> 
                conflictDetectionService.checkConflict(roomId, startTime, endTime, null));
    }
    
    @Test
    @DisplayName("T052-2: 與現有預約完全重疊應該拋出異常")
    void testCheckConflict_ExactOverlap() {
        // Given
        Reservation existingReservation = Reservation.builder()
                .id(1L)
                .roomId(roomId)
                .startTime(startTime)
                .endTime(endTime)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(existingReservation));
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () ->
                conflictDetectionService.checkConflict(roomId, startTime, endTime, null));
        
        assertTrue(exception.getMessage().contains("已被預約"));
    }
    
    @Test
    @DisplayName("T052-3: 與現有預約部分重疊應該拋出異常")
    void testCheckConflict_PartialOverlap() {
        // Given
        Reservation existingReservation = Reservation.builder()
                .id(1L)
                .roomId(roomId)
                .startTime(LocalDateTime.of(2025, 11, 21, 9, 30))
                .endTime(LocalDateTime.of(2025, 11, 21, 10, 30))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(existingReservation));
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        assertThrows(ConflictException.class, () ->
                conflictDetectionService.checkConflict(roomId, startTime, endTime, null));
    }
    
    @Test
    @DisplayName("T052-4: 與維護時段衝突應該拋出異常")
    void testCheckConflict_MaintenanceConflict() {
        // Given
        MaintenanceSchedule maintenanceSchedule = MaintenanceSchedule.builder()
                .id(1L)
                .roomId(roomId)
                .startTime(startTime)
                .endTime(endTime)
                .reason("定期維護")
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(maintenanceSchedule));
        
        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () ->
                conflictDetectionService.checkConflict(roomId, startTime, endTime, null));
        
        assertTrue(exception.getMessage().contains("維護"));
    }
    
    @Test
    @DisplayName("T052-5: 緊鄰時段 (無重疊) 應該通過檢查")
    void testCheckConflict_AdjacentTimeSlots() {
        // Given
        Reservation existingReservation = Reservation.builder()
                .id(1L)
                .roomId(roomId)
                .startTime(LocalDateTime.of(2025, 11, 21, 8, 0))
                .endTime(LocalDateTime.of(2025, 11, 21, 9, 0)) // 結束時間 = 新預約開始時間
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(existingReservation));
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        assertDoesNotThrow(() ->
                conflictDetectionService.checkConflict(roomId, startTime, endTime, null));
    }
    
    @Test
    @DisplayName("T052-6: 更新預約時應該排除自己")
    void testCheckConflict_ExcludeOwnReservation() {
        // Given
        Long excludeReservationId = 1L;
        Reservation ownReservation = Reservation.builder()
                .id(excludeReservationId)
                .roomId(roomId)
                .startTime(startTime)
                .endTime(endTime)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(ownReservation));
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        assertDoesNotThrow(() ->
                conflictDetectionService.checkConflict(roomId, startTime, endTime, excludeReservationId));
    }
    
    @Test
    @DisplayName("T052-7: 已取消的預約不應該算作衝突")
    void testCheckConflict_IgnoreCancelledReservations() {
        // Given
        Reservation cancelledReservation = Reservation.builder()
                .id(1L)
                .roomId(roomId)
                .startTime(startTime)
                .endTime(endTime)
                .status(Reservation.ReservationStatus.CANCELLED)
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(cancelledReservation));
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        assertDoesNotThrow(() ->
                conflictDetectionService.checkConflict(roomId, startTime, endTime, null));
    }
    
    @Test
    @DisplayName("T052-8: 跨天預約應該正確檢測衝突")
    void testCheckConflict_OvernightReservation() {
        // Given
        LocalDateTime overnightStart = LocalDateTime.of(2025, 11, 21, 22, 0);
        LocalDateTime overnightEnd = LocalDateTime.of(2025, 11, 22, 2, 0);
        
        Reservation existingReservation = Reservation.builder()
                .id(1L)
                .roomId(roomId)
                .startTime(LocalDateTime.of(2025, 11, 22, 1, 0))
                .endTime(LocalDateTime.of(2025, 11, 22, 3, 0))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        when(reservationRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of(existingReservation));
        when(maintenanceScheduleRepository.findByRoomIdAndTimeRange(eq(roomId), any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        assertThrows(ConflictException.class, () ->
                conflictDetectionService.checkConflict(roomId, overnightStart, overnightEnd, null));
    }
}
