package tw.huangcti.imrbs.web.mapper;

import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.service.RoomAvailabilityService;
import tw.huangcti.imrbs.web.dto.RoomDTO;
import tw.huangcti.imrbs.web.dto.TimeSlotDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 會議室 DTO 映射器
 */
@Component
public class RoomMapper {

    /**
     * Domain Room → RoomDTO
     */
    public RoomDTO toDTO(Room room) {
        if (room == null) {
            return null;
        }

        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .building(room.getBuilding())
                .floor(room.getFloor())
                .status(room.getStatus().name())
                .equipment(room.getEquipment() != null 
                        ? room.getEquipment().stream()
                                .map(eq -> eq.name() + "(" + eq.quantity() + ")")
                                .collect(Collectors.toList())
                        : List.of())
                .features(room.getFeatures())
                .photos(room.getPhotos())
                .bookingRule(toBookingRuleDTO(room.getBookingRule()))
                .build();
    }

    /**
     * List<Room> → List<RoomDTO>
     */
    public List<RoomDTO> toDTOList(List<Room> rooms) {
        if (rooms == null) {
            return List.of();
        }
        return rooms.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * BookingRule → BookingRuleDTO
     */
    private RoomDTO.BookingRuleDTO toBookingRuleDTO(Room.BookingRule rule) {
        if (rule == null) {
            return null;
        }

        return RoomDTO.BookingRuleDTO.builder()
                .maxHoursPerReservation(rule.maxHoursPerReservation())
                .minAdvanceBookingHours(rule.minAdvanceBookingHours())
                .maxAdvanceBookingDays(rule.maxAdvanceBookingDays())
                .allowRecurring(rule.allowRecurring())
                .requiresApproval(rule.requiresApproval())
                .build();
    }

    /**
     * TimeSlot → TimeSlotDTO
     */
    public TimeSlotDTO toTimeSlotDTO(RoomAvailabilityService.TimeSlot timeSlot) {
        if (timeSlot == null) {
            return null;
        }

        return TimeSlotDTO.builder()
                .startTime(timeSlot.startTime())
                .endTime(timeSlot.endTime())
                .available(timeSlot.available())
                .build();
    }

    /**
     * List<TimeSlot> → List<TimeSlotDTO>
     */
    public List<TimeSlotDTO> toTimeSlotDTOList(List<RoomAvailabilityService.TimeSlot> timeSlots) {
        if (timeSlots == null) {
            return List.of();
        }
        return timeSlots.stream()
                .map(this::toTimeSlotDTO)
                .collect(Collectors.toList());
    }
}
