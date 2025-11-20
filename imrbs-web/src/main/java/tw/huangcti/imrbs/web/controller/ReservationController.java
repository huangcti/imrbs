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
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.web.dto.CreateReservationRequest;
import tw.huangcti.imrbs.web.dto.ReservationDTO;
import tw.huangcti.imrbs.web.mapper.ReservationMapper;

import java.net.URI;

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
}
