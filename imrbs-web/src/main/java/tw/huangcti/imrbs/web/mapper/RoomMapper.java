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

        return new RoomDTO(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getLocationDescription(),
                room.getBuilding(),
                room.getFloor(),
                room.getStatus().name(),
                room.getEquipment() != null 
                        ? room.getEquipment().stream()
                                .map(eq -> eq.getName() + "(" + eq.getQuantity() + ")")
                                .collect(Collectors.toList())
                        : List.of(),
                room.getFeatures(),
                room.getPhotos(),
                toBookingRuleDTO(room.getBookingRule())
        );
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
     * 注意:Domain BookingRule 欄位名與 DTO 不完全對應
     */
    private RoomDTO.BookingRuleDTO toBookingRuleDTO(Room.BookingRule rule) {
        if (rule == null) {
            return null;
        }

        return new RoomDTO.BookingRuleDTO(
                rule.getMaxDurationHours(),           // maxHoursPerReservation
                rule.getMinBookingMinutes(),          // minAdvanceBookingHours (語意不同,需檢查)
                rule.getAdvanceBookingDays(),         // maxAdvanceBookingDays
                false,                                 // allowRecurring (BookingRule沒有此欄位)
                false                                  // requiresApproval (BookingRule沒有此欄位)
        );
    }

    /**
     * TimeSlot → TimeSlotDTO
     */
    public TimeSlotDTO toTimeSlotDTO(RoomAvailabilityService.TimeSlot timeSlot) {
        if (timeSlot == null) {
            return null;
        }

        return new TimeSlotDTO(
                timeSlot.startTime(),
                timeSlot.endTime(),
                timeSlot.available()
        );
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
