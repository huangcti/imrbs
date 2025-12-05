package tw.huangcti.imrbs.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.application.usecase.CreateRoomUseCase;
import tw.huangcti.imrbs.application.usecase.DeleteRoomUseCase;
import tw.huangcti.imrbs.application.usecase.UpdateRoomUseCase;
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
@Tag(name = "Rooms", description = "會議室查詢與管理 API")
@SecurityRequirement(name = "Bearer Authentication")
public class RoomController {

    private final RoomAvailabilityService roomAvailabilityService;
    private final RoomMapper roomMapper;
    
    // T118-T120: US4 會議室 CRUD Use Cases
    private final CreateRoomUseCase createRoomUseCase;
    private final UpdateRoomUseCase updateRoomUseCase;
    private final DeleteRoomUseCase deleteRoomUseCase;
    
    // T122: US4 檔案上傳服務
    private final tw.huangcti.imrbs.infrastructure.integration.FileUploadService fileUploadService;

    /**
     * T058: 查詢可用會議室
     * GET /api/v1/rooms?startTime=...&endTime=...&minCapacity=10
     */
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "查詢可用會議室", description = "根據時間與容量篩選可用會議室")
    public ResponseEntity<Map<String, Object>> getAvailableRooms(
            @Parameter(description = "開始時間", required = true)
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            
            @Parameter(description = "結束時間", required = true)
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            
            @Parameter(description = "最小容量")
            @RequestParam(value = "minCapacity", required = false) Integer minCapacity
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
            @PathVariable("id") Long id
    ) {
        Room room = roomAvailabilityService.findRoomById(id)
                .orElseThrow(() -> new tw.huangcti.imrbs.domain.exception.NotFoundException("會議室不存在"));
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
            @PathVariable("id") Long id,
            
            @Parameter(description = "查詢日期", required = true, example = "2025-12-01")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
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

    /**
     * T118 [P] [US4] 創建會議室
     * POST /api/v1/rooms
     * 權限: ROOM_ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ROOM_ADMIN')")
    @Operation(summary = "創建會議室", description = "管理員創建新會議室 (需 ROOM_ADMIN 權限)")
    public ResponseEntity<RoomDTO> createRoom(
            @RequestBody RoomDTO roomDTO
    ) {
        Room room = roomMapper.toEntity(roomDTO);
        Room createdRoom = createRoomUseCase.execute(room);
        RoomDTO responseDTO = roomMapper.toDTO(createdRoom);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * T119 [P] [US4] 更新會議室
     * PUT /api/v1/rooms/{id}
     * 權限: ROOM_ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROOM_ADMIN')")
    @Operation(summary = "更新會議室", description = "管理員更新會議室資訊 (需 ROOM_ADMIN 權限)")
    public ResponseEntity<RoomDTO> updateRoom(
            @Parameter(description = "會議室 ID", required = true)
            @PathVariable("id") Long id,
            
            @RequestBody RoomDTO roomDTO
    ) {
        Room room = roomMapper.toEntity(roomDTO);
        Room updatedRoom = updateRoomUseCase.execute(id, room);
        RoomDTO responseDTO = roomMapper.toDTO(updatedRoom);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * T120 [P] [US4] 刪除會議室
     * DELETE /api/v1/rooms/{id}
     * 權限: ROOM_ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROOM_ADMIN')")
    @Operation(summary = "刪除會議室", description = "管理員刪除會議室 (軟刪除，需 ROOM_ADMIN 權限)")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "會議室 ID", required = true)
            @PathVariable("id") Long id
    ) {
        deleteRoomUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * T122 [P] [US4] 上傳會議室照片
     * POST /api/v1/rooms/{id}/photos
     * 權限: ROOM_ADMIN
     */
    @PostMapping("/{id}/photos")
    @PreAuthorize("hasRole('ROOM_ADMIN')")
    @Operation(summary = "上傳會議室照片", description = "管理員上傳會議室照片 (需 ROOM_ADMIN 權限)")
    public ResponseEntity<Map<String, String>> uploadRoomPhoto(
            @Parameter(description = "會議室 ID", required = true)
            @PathVariable("id") Long id,
            
            @Parameter(description = "照片檔案 (JPEG/PNG/WebP, 最大 5MB)", required = true)
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        try {
            String photoUrl = fileUploadService.uploadRoomPhoto(file, id);
            return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
        } catch (java.io.IOException e) {
            throw new tw.huangcti.imrbs.domain.exception.ValidationException(
                "檔案上傳失敗: " + e.getMessage()
            );
        }
    }
}
