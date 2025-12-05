package tw.huangcti.imrbs.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tw.huangcti.imrbs.application.usecase.ApproveGuestRequestUseCase;
import tw.huangcti.imrbs.application.usecase.CreateGuestRequestUseCase;
import tw.huangcti.imrbs.application.usecase.RejectGuestRequestUseCase;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 訪客預約管理 REST API
 * 
 * 包含公開端點 (訪客提交/查詢) 和管理端點 (審核)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Guest Requests", description = "訪客預約申請管理 API")
public class GuestController {

    private final CreateGuestRequestUseCase createGuestRequestUseCase;
    private final ApproveGuestRequestUseCase approveGuestRequestUseCase;
    private final RejectGuestRequestUseCase rejectGuestRequestUseCase;
    private final GuestReservationRequestRepository guestReservationRequestRepository;

    // ============================================
    // 公開端點 (無需認證)
    // ============================================

    /**
     * T155: 訪客提交預約申請
     * POST /api/guest/requests
     */
    @PostMapping("/guest/requests")
    @Operation(summary = "訪客提交預約申請", description = "外部訪客提交會議室預約申請，無需登入")
    public ResponseEntity<GuestRequestResponse> createGuestRequest(
            @Valid @RequestBody CreateGuestRequestRequest request
    ) {
        try {
            // 轉換為 Use Case Command
            CreateGuestRequestUseCase.CreateGuestRequestCommand command =
                    new CreateGuestRequestUseCase.CreateGuestRequestCommand(
                            request.getGuestName(),
                            request.getGuestEmail(),
                            request.getGuestPhone(),
                            request.getGuestCompany(),
                            request.getRoomId(),
                            request.getMeetingTitle(),
                            request.getRequestedStartTime(),
                            request.getRequestedEndTime(),
                            request.getAttendeeCount(),
                            request.getPurpose()
                    );

            // 執行用例
            GuestReservationRequest created = createGuestRequestUseCase.execute(command);

            // 構建 Location header
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.getId())
                    .toUri();

            // 201 Created 回應
            return ResponseEntity
                    .created(location)
                    .body(toResponse(created));
        } catch (IllegalStateException e) {
            // 時段衝突
            throw new ConflictException(e.getMessage());
        }
    }

    /**
     * 訪客查詢自己的申請狀態
     * GET /api/guest/requests/{id}
     */
    @GetMapping("/guest/requests/{id}")
    @Operation(summary = "訪客查詢申請狀態", description = "訪客可透過申請 ID 和 Email 查詢自己的申請狀態")
    public ResponseEntity<GuestRequestResponse> getGuestRequestStatus(
            @PathVariable Long id,
            @RequestParam @Email String email
    ) {
        // 查詢申請
        GuestReservationRequest request = guestReservationRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("申請不存在"));

        // 驗證 Email 是否匹配
        if (!request.getGuestEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("您無權查看此申請");
        }

        return ResponseEntity.ok(toResponse(request));
    }

    // ============================================
    // 管理端點 (需 ROOM_ADMIN 權限)
    // ============================================

    /**
     * T156: 管理員查詢訪客申請清單
     * GET /api/admin/guest-requests
     */
    @GetMapping("/admin/guest-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "查詢訪客申請清單", description = "管理員查詢所有訪客預約申請")
    public ResponseEntity<List<GuestRequestResponse>> getGuestRequests(
            @RequestParam(required = false) String status
    ) {
        List<GuestReservationRequest> requests;

        if (status != null && !status.isEmpty()) {
            try {
                GuestReservationRequest.RequestStatus requestStatus =
                        GuestReservationRequest.RequestStatus.valueOf(status.toUpperCase());
                requests = guestReservationRequestRepository.findByStatus(requestStatus);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("無效的狀態: " + status);
            }
        } else {
            requests = guestReservationRequestRepository.findAll();
        }

        List<GuestRequestResponse> responses = requests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * T157: 管理員批准訪客申請
     * POST /api/admin/guest-requests/{id}/approve
     */
    @PostMapping("/admin/guest-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "批准訪客申請", description = "管理員批准訪客預約申請，自動創建正式預約")
    public ResponseEntity<GuestRequestResponse> approveGuestRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            String username = jwt != null ? jwt.getSubject() : "admin";

            GuestReservationRequest approved = approveGuestRequestUseCase.execute(id, username);
            return ResponseEntity.ok(toResponse(approved));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * T158: 管理員拒絕訪客申請
     * POST /api/admin/guest-requests/{id}/reject
     */
    @PostMapping("/admin/guest-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "拒絕訪客申請", description = "管理員拒絕訪客預約申請，需提供拒絕原因")
    public ResponseEntity<GuestRequestResponse> rejectGuestRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectGuestRequestRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            String username = jwt != null ? jwt.getSubject() : "admin";

            GuestReservationRequest rejected = rejectGuestRequestUseCase.execute(
                    id,
                    username,
                    request.getReason()
            );
            return ResponseEntity.ok(toResponse(rejected));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    // ============================================
    // DTO 和轉換方法
    // ============================================

    /**
     * 轉換為回應 DTO
     */
    private GuestRequestResponse toResponse(GuestReservationRequest request) {
        GuestRequestResponse response = new GuestRequestResponse();
        response.setId(request.getId());
        response.setGuestName(request.getGuestName());
        response.setGuestEmail(request.getGuestEmail());
        response.setGuestPhone(request.getGuestPhone());
        response.setGuestCompany(request.getGuestCompany());
        response.setRoomId(request.getRoomId());
        response.setMeetingTitle(request.getMeetingTitle());
        response.setRequestedStartTime(request.getRequestedStartTime());
        response.setRequestedEndTime(request.getRequestedEndTime());
        response.setMeetingPurpose(request.getMeetingPurpose());
        response.setStatus(request.getStatus() != null ? request.getStatus().name() : "PENDING");
        response.setReviewedBy(request.getReviewedBy());
        response.setReviewedAt(request.getReviewedAt());
        response.setRejectionReason(request.getRejectionReason());
        response.setReservationId(request.getReservationId());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }

    // ============================================
    // 請求/回應 DTO
    // ============================================

    @Data
    public static class CreateGuestRequestRequest {
        @NotBlank(message = "訪客姓名不能為空")
        private String guestName;

        @NotBlank(message = "訪客 Email 不能為空")
        @Email(message = "Email 格式無效")
        private String guestEmail;

        private String guestPhone;

        private String guestCompany;

        @NotNull(message = "會議室 ID 不能為空")
        private Long roomId;

        @NotBlank(message = "會議標題不能為空")
        private String meetingTitle;

        @NotNull(message = "開始時間不能為空")
        private LocalDateTime requestedStartTime;

        @NotNull(message = "結束時間不能為空")
        private LocalDateTime requestedEndTime;

        private Integer attendeeCount;

        private String purpose;
    }

    @Data
    public static class RejectGuestRequestRequest {
        @NotBlank(message = "拒絕原因不能為空")
        private String reason;
    }

    @Data
    public static class GuestRequestResponse {
        private Long id;
        private String guestName;
        private String guestEmail;
        private String guestPhone;
        private String guestCompany;
        private Long roomId;
        private String meetingTitle;
        private LocalDateTime requestedStartTime;
        private LocalDateTime requestedEndTime;
        private String meetingPurpose;
        private String status;
        private Long reviewedBy;
        private LocalDateTime reviewedAt;
        private String rejectionReason;
        private Long reservationId;
        private LocalDateTime createdAt;
        // 用於測試兼容性
        private String approvedBy;
        private LocalDateTime approvedAt;
    }

    // ============================================
    // 自訂異常類別 (用於 HTTP 狀態碼映射)
    // ============================================

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
