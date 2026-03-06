package tw.huangcti.imrbs.domain.service;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RoomAvailabilityService - 會議室可用性查詢服務 (Domain Service - Framework Agnostic)
 */
@RequiredArgsConstructor
public class RoomAvailabilityService {
    
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    
    public List<Room> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime, Integer minCapacity) {
        return findAvailableRooms(startTime, endTime, minCapacity, null);
    }
    
    public List<Room> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime, Integer minCapacity, String name) {
        List<Room> rooms = roomRepository.findAll();
        
        return rooms.stream()
                .filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE)
                .filter(room -> minCapacity == null || room.getCapacity() >= minCapacity)
                .filter(room -> name == null || name.isBlank() || room.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(room -> !hasConflict(room.getId(), startTime, endTime))
                .collect(Collectors.toList());
    }
    
    public Optional<Room> findRoomById(Long id) {
        return roomRepository.findById(id);
    }
    
    public List<TimeSlot> getAvailableTimeSlots(Long roomId, LocalDate date) {
        LocalDateTime dayStart = date.atTime(LocalTime.MIN);
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        
        var reservations = reservationRepository.findByRoomIdAndTimeRange(roomId, dayStart, dayEnd);
        var maintenances = maintenanceScheduleRepository.findByRoomIdAndTimeRange(roomId, dayStart, dayEnd);
        
        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime current = date.atTime(8, 0);
        LocalDateTime endOfDay = date.atTime(18, 0);
        
        while (current.isBefore(endOfDay)) {
            LocalDateTime slotEnd = current.plusHours(1);
            boolean available = !hasConflictInTimeRange(roomId, current, slotEnd, reservations, maintenances);
            slots.add(new TimeSlot(current, slotEnd, available));
            current = slotEnd;
        }
        
        return slots;
    }
    
    private boolean hasConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        var reservations = reservationRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime);
        var maintenances = maintenanceScheduleRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime);
        
        return !reservations.isEmpty() || !maintenances.isEmpty();
    }
    
    private boolean hasConflictInTimeRange(Long roomId, LocalDateTime start, LocalDateTime end,
                                           List<?> reservations, List<?> maintenances) {
        return !reservations.isEmpty() || !maintenances.isEmpty();
    }
    
    public record TimeSlot(LocalDateTime startTime, LocalDateTime endTime, boolean available) {}
}
