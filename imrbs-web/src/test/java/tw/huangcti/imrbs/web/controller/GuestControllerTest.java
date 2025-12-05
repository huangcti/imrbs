package tw.huangcti.imrbs.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tw.huangcti.imrbs.application.usecase.ApproveGuestRequestUseCase;
import tw.huangcti.imrbs.application.usecase.CreateGuestRequestUseCase;
import tw.huangcti.imrbs.application.usecase.RejectGuestRequestUseCase;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 訪客預約控制器測試
 * 
 * 測試場景:
 * 1. 訪客提交預約申請 (公開端點)
 * 2. 管理員查詢待審核申請
 * 3. 管理員批准申請
 * 4. 管理員拒絕申請
 */
@WebMvcTest(GuestController.class)
@DisplayName("GuestController 訪客預約測試")
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateGuestRequestUseCase createGuestRequestUseCase;

    @MockBean
    private ApproveGuestRequestUseCase approveGuestRequestUseCase;

    @MockBean
    private RejectGuestRequestUseCase rejectGuestRequestUseCase;

    @MockBean
    private GuestReservationRequestRepository guestReservationRequestRepository;

    private GuestReservationRequest sampleGuestRequest;

    @BeforeEach
    void setUp() {
        sampleGuestRequest = GuestReservationRequest.builder()
                .id(1L)
                .guestName("訪客張三")
                .guestEmail("guest@external.com")
                .guestPhone("0912-345-678")
                .guestCompany("外部公司 A")
                .roomId(1L)
                .meetingTitle("客戶會議")
                .requestedStartTime(LocalDateTime.of(2024, 3, 15, 10, 0))
                .requestedEndTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .meetingPurpose("業務洽談")
                .status(GuestReservationRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/guest/requests - 訪客提交預約申請")
    class CreateGuestRequest {

        @Test
        @DisplayName("成功提交訪客預約申請")
        void shouldCreateGuestRequestSuccessfully() throws Exception {
            // Given
            CreateGuestRequestRequest request = new CreateGuestRequestRequest();
            request.setGuestName("訪客張三");
            request.setGuestEmail("guest@external.com");
            request.setGuestPhone("0912-345-678");
            request.setGuestCompany("外部公司 A");
            request.setRoomId(1L);
            request.setMeetingTitle("客戶會議");
            request.setRequestedStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setRequestedEndTime(LocalDateTime.of(2024, 3, 15, 12, 0));
            request.setAttendeeCount(5);
            request.setPurpose("業務洽談");

            when(createGuestRequestUseCase.execute(any())).thenReturn(sampleGuestRequest);

            // When & Then
            mockMvc.perform(post("/api/guest/requests")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.guestName").value("訪客張三"))
                    .andExpect(jsonPath("$.guestEmail").value("guest@external.com"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("訪客資訊不完整時返回 400")
        void shouldReturn400WhenGuestInfoIncomplete() throws Exception {
            // Given - 缺少必填欄位
            CreateGuestRequestRequest request = new CreateGuestRequestRequest();
            request.setGuestEmail("guest@external.com");
            // 缺少 guestName, roomId, meetingTitle 等

            // When & Then
            mockMvc.perform(post("/api/guest/requests")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("無效的 Email 格式返回 400")
        void shouldReturn400WhenInvalidEmail() throws Exception {
            // Given
            CreateGuestRequestRequest request = new CreateGuestRequestRequest();
            request.setGuestName("訪客張三");
            request.setGuestEmail("invalid-email"); // 無效格式
            request.setRoomId(1L);
            request.setMeetingTitle("客戶會議");
            request.setRequestedStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setRequestedEndTime(LocalDateTime.of(2024, 3, 15, 12, 0));

            // When & Then
            mockMvc.perform(post("/api/guest/requests")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("時間衝突時返回 409")
        void shouldReturn409WhenTimeConflict() throws Exception {
            // Given
            CreateGuestRequestRequest request = new CreateGuestRequestRequest();
            request.setGuestName("訪客張三");
            request.setGuestEmail("guest@external.com");
            request.setRoomId(1L);
            request.setMeetingTitle("客戶會議");
            request.setRequestedStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setRequestedEndTime(LocalDateTime.of(2024, 3, 15, 12, 0));

            when(createGuestRequestUseCase.execute(any()))
                    .thenThrow(new IllegalStateException("該時段已有預約"));

            // When & Then
            mockMvc.perform(post("/api/guest/requests")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/guest-requests - 管理員查詢待審核申請")
    class GetPendingGuestRequests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("管理員成功取得待審核清單")
        void shouldReturnPendingGuestRequestsForAdmin() throws Exception {
            // Given
            List<GuestReservationRequest> requests = Arrays.asList(sampleGuestRequest);
            when(guestReservationRequestRepository.findByStatus(
                    GuestReservationRequest.RequestStatus.PENDING))
                    .thenReturn(requests);

            // When & Then
            mockMvc.perform(get("/api/admin/guest-requests")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].guestName").value("訪客張三"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("查詢所有狀態的訪客申請")
        void shouldReturnAllGuestRequestsForAdmin() throws Exception {
            // Given
            GuestReservationRequest approvedRequest = GuestReservationRequest.builder()
                    .id(2L)
                    .guestName("訪客李四")
                    .guestEmail("guest2@external.com")
                    .status(GuestReservationRequest.RequestStatus.APPROVED)
                    .build();

            List<GuestReservationRequest> requests = Arrays.asList(sampleGuestRequest, approvedRequest);
            when(guestReservationRequestRepository.findAll()).thenReturn(requests);

            // When & Then
            mockMvc.perform(get("/api/admin/guest-requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("未認證用戶無法存取管理員端點")
        void shouldReturn401ForUnauthenticatedUser() throws Exception {
            mockMvc.perform(get("/api/admin/guest-requests"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("一般使用者無權存取管理員端點")
        void shouldReturn403ForNonAdminUser() throws Exception {
            mockMvc.perform(get("/api/admin/guest-requests"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/guest-requests/{id}/approve - 批准訪客申請")
    class ApproveGuestRequest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("管理員成功批准訪客申請")
        void shouldApproveGuestRequestSuccessfully() throws Exception {
            // Given
            GuestReservationRequest approvedRequest = GuestReservationRequest.builder()
                    .id(1L)
                    .guestName("訪客張三")
                    .guestEmail("guest@external.com")
                    .status(GuestReservationRequest.RequestStatus.APPROVED)
                    .reviewedBy(100L)
                    .reviewedAt(LocalDateTime.now())
                    .build();

            when(approveGuestRequestUseCase.execute(eq(1L), anyString())).thenReturn(approvedRequest);

            // When & Then
            mockMvc.perform(post("/api/admin/guest-requests/1/approve")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.reviewedBy").value(100));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("批准不存在的申請返回 404")
        void shouldReturn404WhenRequestNotFound() throws Exception {
            // Given
            when(approveGuestRequestUseCase.execute(eq(999L), anyString()))
                    .thenThrow(new IllegalArgumentException("申請不存在"));

            // When & Then
            mockMvc.perform(post("/api/admin/guest-requests/999/approve")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("批准已處理的申請返回 400")
        void shouldReturn400WhenRequestAlreadyProcessed() throws Exception {
            // Given
            when(approveGuestRequestUseCase.execute(eq(1L), anyString()))
                    .thenThrow(new IllegalStateException("該申請已被處理"));

            // When & Then
            mockMvc.perform(post("/api/admin/guest-requests/1/approve")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/guest-requests/{id}/reject - 拒絕訪客申請")
    class RejectGuestRequest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("管理員成功拒絕訪客申請")
        void shouldRejectGuestRequestSuccessfully() throws Exception {
            // Given
            GuestReservationRequest rejectedRequest = GuestReservationRequest.builder()
                    .id(1L)
                    .guestName("訪客張三")
                    .guestEmail("guest@external.com")
                    .status(GuestReservationRequest.RequestStatus.REJECTED)
                    .rejectionReason("會議室已滿")
                    .build();

            RejectGuestRequestRequest request = new RejectGuestRequestRequest();
            request.setReason("會議室已滿");

            when(rejectGuestRequestUseCase.execute(eq(1L), anyString(), anyString()))
                    .thenReturn(rejectedRequest);

            // When & Then
            mockMvc.perform(post("/api/admin/guest-requests/1/reject")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("REJECTED"))
                    .andExpect(jsonPath("$.rejectionReason").value("會議室已滿"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("拒絕申請必須提供原因")
        void shouldRequireReasonWhenRejecting() throws Exception {
            // Given - 沒有提供拒絕原因
            RejectGuestRequestRequest request = new RejectGuestRequestRequest();
            // reason 為空

            // When & Then
            mockMvc.perform(post("/api/admin/guest-requests/1/reject")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/guest/requests/{id} - 訪客查詢申請狀態")
    class GetGuestRequestStatus {

        @Test
        @DisplayName("訪客可以查詢自己的申請狀態")
        void shouldReturnGuestRequestStatus() throws Exception {
            // Given
            when(guestReservationRequestRepository.findById(1L))
                    .thenReturn(Optional.of(sampleGuestRequest));

            // When & Then
            mockMvc.perform(get("/api/guest/requests/1")
                            .param("email", "guest@external.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("Email 不符時返回 403")
        void shouldReturn403WhenEmailNotMatch() throws Exception {
            // Given
            when(guestReservationRequestRepository.findById(1L))
                    .thenReturn(Optional.of(sampleGuestRequest));

            // When & Then
            mockMvc.perform(get("/api/guest/requests/1")
                            .param("email", "wrong@email.com"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("申請不存在時返回 404")
        void shouldReturn404WhenRequestNotFound() throws Exception {
            // Given
            when(guestReservationRequestRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/guest/requests/999")
                            .param("email", "guest@external.com"))
                    .andExpect(status().isNotFound());
        }
    }

    // DTO 內部類別 (實際實作時應該在獨立檔案)
    static class CreateGuestRequestRequest {
        private String guestName;
        private String guestEmail;
        private String guestPhone;
        private String guestCompany;
        private Long roomId;
        private String meetingTitle;
        private LocalDateTime requestedStartTime;
        private LocalDateTime requestedEndTime;
        private Integer attendeeCount;
        private String purpose;

        // Getters and Setters
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getGuestEmail() { return guestEmail; }
        public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
        public String getGuestPhone() { return guestPhone; }
        public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
        public String getGuestCompany() { return guestCompany; }
        public void setGuestCompany(String guestCompany) { this.guestCompany = guestCompany; }
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        public String getMeetingTitle() { return meetingTitle; }
        public void setMeetingTitle(String meetingTitle) { this.meetingTitle = meetingTitle; }
        public LocalDateTime getRequestedStartTime() { return requestedStartTime; }
        public void setRequestedStartTime(LocalDateTime requestedStartTime) { this.requestedStartTime = requestedStartTime; }
        public LocalDateTime getRequestedEndTime() { return requestedEndTime; }
        public void setRequestedEndTime(LocalDateTime requestedEndTime) { this.requestedEndTime = requestedEndTime; }
        public Integer getAttendeeCount() { return attendeeCount; }
        public void setAttendeeCount(Integer attendeeCount) { this.attendeeCount = attendeeCount; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }

    static class RejectGuestRequestRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
