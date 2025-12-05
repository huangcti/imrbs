package tw.huangcti.imrbs.domain.service;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 預約衝突檢測服務 (Domain Service - Framework Agnostic)
 */
@RequiredArgsConstructor
public class ConflictDetectionService {
    
    private final ReservationRepository reservationRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    
    /**
     * 檢查衝突並拋出異常 (原有方法)
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param excludeReservationId 排除的預約 ID (更新時使用)
     * @throws ConflictException 如果有衝突
     */
    public void checkConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId) {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime);
        
        boolean hasReservationConflict = reservations.stream()
                .filter(r -> !r.getId().equals(excludeReservationId))
                .anyMatch(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED);
        
        if (hasReservationConflict) {
            throw new ConflictException("會議室在此時段已被預約");
        }
        
        List<MaintenanceSchedule> maintenances = maintenanceScheduleRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime);
        if (!maintenances.isEmpty()) {
            throw new ConflictException("會議室在此時段正在維護");
        }
    }
    
    /**
     * 檢查是否有衝突 (返回布林值)
     * 適用於訪客預約申請等需要非拋異常方式檢查的場景
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param excludeReservationId 排除的預約 ID (更新時使用)
     * @return true 如果有衝突，false 如果無衝突
     */
    public boolean hasConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId) {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime);
        
        boolean hasReservationConflict = reservations.stream()
                .filter(r -> excludeReservationId == null || !r.getId().equals(excludeReservationId))
                .anyMatch(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED);
        
        if (hasReservationConflict) {
            return true;
        }
        
        List<MaintenanceSchedule> maintenances = maintenanceScheduleRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime);
        return !maintenances.isEmpty();
    }
}
