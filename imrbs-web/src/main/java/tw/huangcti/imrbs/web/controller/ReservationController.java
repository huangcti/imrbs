package tw.huangcti.imrbs.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tw.huangcti.imrbs.application.usecase.CreateReservationUseCase;
import tw.huangcti.imrbs.application.usecase.UpdateReservationUseCase;
import tw.huangcti.imrbs.application.usecase.CancelReservationUseCase;
import tw.huangcti.imrbs.application.usecase.GetReservationUseCase;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.web.dto.CreateReservationRequest;
import tw.huangcti.imrbs.web.dto.UpdateReservationRequest;
import tw.huangcti.imrbs.web.dto.CancelReservationRequest;
import tw.huangcti.imrbs.web.dto.ReservationDTO;
import tw.huangcti.imrbs.web.mapper.ReservationMapper;
import tw.huangcti.imrbs.domain.exception.NotFoundException;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 預約管理 REST API
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "預約管理 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final UpdateReservationUseCase updateReservationUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final GetReservationUseCase getReservationUseCase;
    private final ReservationMapper reservationMapper;

    /**
     * T061: 創建預約
     * POST /api/v1/reservations
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "創建預約", description = "員工提交會議室預約申請")
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        // 轉換為 Use Case Command
        CreateReservationUseCase.CreateReservationCommand command = 
                new CreateReservationUseCase.CreateReservationCommand(
                        request.roomId(),
                        request.userId(),
                        request.meetingTitle(),
                        request.startTime(),
                        request.endTime(),
                        request.participants(),
                        request.recurringRule()
                );

        // 執行用例
        Reservation reservation = createReservationUseCase.execute(command);

        // 轉換為 DTO
        ReservationDTO dto = reservationMapper.toDTO(reservation);

        // 構建 Location header
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();

        // 201 Created 回應
        return ResponseEntity
                .created(location)
                .body(dto);
    }
    
    /**
     * T083: 查詢預約清單 (當前使用者)
     * GET /api/v1/reservations
     */
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "查詢我的預約", description = "查詢當前使用者的所有預約")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // TODO: 實作查詢邏輯,從 JWT 取得 userId
        // 目前返回空列表
        return ResponseEntity.ok(List.of());
    }
    
    /**
     * T084: 查詢單一預約
     * GET /api/v1/reservations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "查詢預約詳情", description = "查詢指定預約的詳細資訊")
    public ResponseEntity<ReservationDTO> getReservation(
            @PathVariable Long id
    ) {
        Reservation reservation = getReservationUseCase.execute(id)
                .orElseThrow(() -> new NotFoundException("預約不存在"));
        
        ReservationDTO dto = reservationMapper.toDTO(reservation);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * T085: 修改預約
     * PUT /api/v1/reservations/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "修改預約", description = "修改預約時間、標題或參與者")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request
    ) {
        // TODO: 從 JWT 取得 userId
        Long userId = 1001L; // 暫時硬編碼
        
        // 轉換為 Use Case Request
        UpdateReservationUseCase.UpdateRequest updateRequest = 
                new UpdateReservationUseCase.UpdateRequest(
                        userId,
                        request.meetingTitle(),
                        request.startTime(),
                        request.endTime(),
                        request.participants()
                );
        
        // 執行用例
        Reservation updated = updateReservationUseCase.execute(id, updateRequest);
        
        // 轉換為 DTO
        ReservationDTO dto = reservationMapper.toDTO(updated);
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * T086: 取消預約
     * DELETE /api/v1/reservations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "取消預約", description = "取消指定的預約(需符合 24 小時規則)")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @Valid @RequestBody CancelReservationRequest request
    ) {
        // TODO: 從 JWT 取得 userId
        Long userId = 1001L; // 暫時硬編碼
        
        // 執行用例
        cancelReservationUseCase.execute(id, userId, request.cancellationReason());
        
        // 204 No Content
        return ResponseEntity.noContent().build();
    }
}
