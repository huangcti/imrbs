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
}
