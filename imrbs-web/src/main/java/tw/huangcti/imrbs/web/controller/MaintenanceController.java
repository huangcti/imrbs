package tw.huangcti.imrbs.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.application.usecase.CreateMaintenanceScheduleUseCase;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.web.dto.MaintenanceScheduleDTO;
import tw.huangcti.imrbs.web.mapper.MaintenanceScheduleMapper;

/**
 * T121 [P] [US4] 會議室維護管理 REST API
 * 職責: 處理維護時段的創建、查詢等操作
 * 權限: ROOM_ADMIN 或 SYSTEM_ADMIN
 */
@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "會議室維護管理 API")
@SecurityRequirement(name = "Bearer Authentication")
public class MaintenanceController {

    private final CreateMaintenanceScheduleUseCase createMaintenanceScheduleUseCase;
    private final MaintenanceScheduleMapper maintenanceScheduleMapper;
    private final UserRepository userRepository;

    /**
     * T121 [P] [US4] 創建維護時段
     * POST /api/v1/admin/rooms/{roomId}/maintenance
     * 權限: ROOM_ADMIN 或 SYSTEM_ADMIN
     */
    @PostMapping("/{roomId}/maintenance")
    @PreAuthorize("hasAnyRole('ROOM_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(
        summary = "創建會議室維護時段", 
        description = "管理員為指定會議室創建維護時段 (需 ROOM_ADMIN 或 SYSTEM_ADMIN 權限)"
    )
    public ResponseEntity<MaintenanceScheduleDTO> createMaintenance(
            @Parameter(description = "會議室 ID", required = true)
            @PathVariable("roomId") Long roomId,
            
            @RequestBody MaintenanceScheduleDTO maintenanceDTO,
            
            @AuthenticationPrincipal Jwt jwt
    ) {
        // 從 JWT 取得使用者資訊
        String employeeId = jwt.getClaim("sub");
        Long userId = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new tw.huangcti.imrbs.domain.exception.NotFoundException(
                    "使用者不存在: " + employeeId
                ))
                .getId();

        // 將 DTO 轉為領域模型
        MaintenanceSchedule maintenanceSchedule = maintenanceScheduleMapper.toEntity(maintenanceDTO)
                .toBuilder()
                .roomId(roomId)
                .createdBy(userId)
                .build();

        // 執行用例
        MaintenanceSchedule createdSchedule = createMaintenanceScheduleUseCase.execute(maintenanceSchedule);

        // 回傳 DTO
        MaintenanceScheduleDTO responseDTO = maintenanceScheduleMapper.toDTO(createdSchedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
