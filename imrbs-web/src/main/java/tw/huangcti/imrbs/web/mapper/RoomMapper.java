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
    
    /**
     * RoomDTO → Domain Room
     * 用於創建/更新會議室
     */
    public Room toEntity(RoomDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Room.builder()
                .id(dto.id())
                .name(dto.name())
                .capacity(dto.capacity())
                .locationDescription(dto.location())
                .building(dto.building())
                .floor(dto.floor())
                .status(dto.status() != null 
                        ? Room.RoomStatus.valueOf(dto.status()) 
                        : Room.RoomStatus.AVAILABLE)
                .equipment(toEquipmentList(dto.equipment()))
                .features(dto.features() != null ? dto.features() : List.of())
                .photos(dto.photos() != null ? dto.photos() : List.of())
                .bookingRule(toBookingRule(dto.bookingRule()))
                .build();
    }
    
    /**
     * List<String> → List<Equipment>
     * 解析設備字串清單 (格式: "設備名稱(數量)")
     */
    private List<Room.Equipment> toEquipmentList(List<String> equipmentStrings) {
        if (equipmentStrings == null || equipmentStrings.isEmpty()) {
            return List.of();
        }
        
        return equipmentStrings.stream()
                .map(this::parseEquipment)
                .collect(Collectors.toList());
    }
    
    /**
     * 解析單一設備字串
     * 格式: "設備名稱(數量)" 或 "設備名稱"
     */
    private Room.Equipment parseEquipment(String equipmentStr) {
        if (equipmentStr == null || equipmentStr.isBlank()) {
            return Room.Equipment.builder().name("").quantity(1).build();
        }
        
        // 嘗試解析格式: "設備名稱(數量)"
        int parenStart = equipmentStr.lastIndexOf('(');
        int parenEnd = equipmentStr.lastIndexOf(')');
        
        if (parenStart > 0 && parenEnd > parenStart) {
            String name = equipmentStr.substring(0, parenStart).trim();
            String quantityStr = equipmentStr.substring(parenStart + 1, parenEnd).trim();
            try {
                int quantity = Integer.parseInt(quantityStr);
                return Room.Equipment.builder()
                        .name(name)
                        .quantity(quantity)
                        .build();
            } catch (NumberFormatException e) {
                // 如果數量解析失敗，把整個字串當作名稱
            }
        }
        
        // 沒有數量標記，預設數量為 1
        return Room.Equipment.builder()
                .name(equipmentStr.trim())
                .quantity(1)
                .build();
    }
    
    /**
     * BookingRuleDTO → Domain BookingRule
     */
    private Room.BookingRule toBookingRule(RoomDTO.BookingRuleDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Room.BookingRule.builder()
                .maxDurationHours(dto.maxHoursPerReservation())
                .minBookingMinutes(dto.minAdvanceBookingHours() != null 
                        ? dto.minAdvanceBookingHours() * 60 : null)  // 小時轉分鐘
                .advanceBookingDays(dto.maxAdvanceBookingDays())
                .build();
    }
}
