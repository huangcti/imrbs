package tw.huangcti.imrbs.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.service.RoomAvailabilityService;
import tw.huangcti.imrbs.web.dto.RoomDTO;
import tw.huangcti.imrbs.web.dto.TimeSlotDTO;
import tw.huangcti.imrbs.web.mapper.RoomMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 會議室查詢 REST API
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "會議室查詢 API")
@SecurityRequirement(name = "Bearer Authentication")
public class RoomController {

    private final RoomAvailabilityService roomAvailabilityService;
    private final RoomMapper roomMapper;

    /**
     * T058: 查詢可用會議室
     * GET /api/v1/rooms?startTime=...&endTime=...&minCapacity=10
     */
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "查詢可用會議室", description = "根據時間與容量篩選可用會議室")
    public ResponseEntity<Map<String, Object>> getAvailableRooms(
            @Parameter(description = "開始時間", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            
            @Parameter(description = "結束時間", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            
            @Parameter(description = "最小容量")
            @RequestParam(required = false) Integer minCapacity
    ) {
        List<Room> rooms = roomAvailabilityService.findAvailableRooms(startTime, endTime, minCapacity);
        List<RoomDTO> roomDTOs = roomMapper.toDTOList(rooms);

        return ResponseEntity.ok(Map.of(
                "data", roomDTOs,
                "total", roomDTOs.size()
        ));
    }

    /**
     * T059: 查詢單一會議室詳情
     * GET /api/v1/rooms/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "查詢會議室詳情", description = "取得指定會議室的詳細資訊")
    public ResponseEntity<RoomDTO> getRoomById(
            @Parameter(description = "會議室 ID", required = true)
            @PathVariable Long id
    ) {
        Room room = roomAvailabilityService.findRoomById(id);
        RoomDTO roomDTO = roomMapper.toDTO(room);
        return ResponseEntity.ok(roomDTO);
    }

    /**
     * T060: 查詢會議室可用時段
     * GET /api/v1/rooms/{id}/availability?date=2025-12-01
     */
    @GetMapping("/{id}/availability")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "查詢會議室可用時段", description = "取得指定日期的可用時段 (8am-6pm)")
    public ResponseEntity<Map<String, Object>> getRoomAvailability(
            @Parameter(description = "會議室 ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "查詢日期", required = true, example = "2025-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<RoomAvailabilityService.TimeSlot> timeSlots = 
                roomAvailabilityService.getAvailableTimeSlots(id, date);
        List<TimeSlotDTO> timeSlotDTOs = roomMapper.toTimeSlotDTOList(timeSlots);

        return ResponseEntity.ok(Map.of(
                "date", date.toString(),
                "roomId", id,
                "availableSlots", timeSlotDTOs
        ));
    }
}
