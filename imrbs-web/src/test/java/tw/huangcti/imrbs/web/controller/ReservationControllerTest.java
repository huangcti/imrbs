package tw.huangcti.imrbs.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tw.huangcti.imrbs.application.usecase.CreateReservationUseCase;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.web.dto.CreateReservationRequest;
import tw.huangcti.imrbs.web.mapper.ReservationMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReservationControllerTest - 預約 API 合約測試 (TDD)
 * 
 * 測試場景:
 * - POST /api/v1/reservations - 創建新預約
 * - 驗證預約時段衝突檢測
 * - 驗證參與者人數限制
 * - 驗證必填欄位
 */
@WebMvcTest(
        controllers = {
                ReservationController.class,
                tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class
        },
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ReservationController.class,
                        tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class,
                        tw.huangcti.imrbs.web.mapper.RoomMapper.class,
                        tw.huangcti.imrbs.web.mapper.ReservationMapper.class
                }
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@Import(TestSecurityConfig.class)
@DisplayName("US1: 預約創建 API 測試")
class ReservationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CreateReservationUseCase createReservationUseCase;

    @MockBean
    private ReservationMapper reservationMapper;    private CreateReservationRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testRequest = new CreateReservationRequest(
                Long.valueOf(1), // roomId
                Long.valueOf(1001), // userId
                "團隊週會", // meetingTitle
                LocalDateTime.of(2025, 11, 21, 9, 0), // startTime
                LocalDateTime.of(2025, 11, 21, 10, 0), // endTime
                "user1@example.com,user2@example.com", // participants
                null // recurringRule
        );
    }
    
    @Test
    @WithMockUser(username = "emp001", roles = "EMPLOYEE")
    @DisplayName("T051-1: 應該成功創建預約")
    void testCreateReservation_Success() throws Exception {
        // Given
        Reservation createdReservation = Reservation.builder()
                .id(1L)
                .roomId(1L)
                .userId(1001L) // 修正為 Long 類型
                .startTime(testRequest.startTime())
                .endTime(testRequest.endTime())
                .meetingTitle(testRequest.meetingTitle())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
        
        tw.huangcti.imrbs.web.dto.ReservationDTO reservationDTO = 
                new tw.huangcti.imrbs.web.dto.ReservationDTO(
                        1L,                             // id
                        1L,                             // roomId
                        "Conference Room A",            // roomName
                        1001L,                          // userId
                        "emp001",                       // userName
                        testRequest.meetingTitle(),     // meetingTitle
                        testRequest.startTime(),        // startTime
                        testRequest.endTime(),          // endTime
                        testRequest.participants(),     // participants
                        "CONFIRMED",                    // status
                        false,                          // isRecurring
                        null,                           // recurringRule
                        LocalDateTime.now()             // createdAt
                );
        
        when(createReservationUseCase.execute(any()))
                .thenReturn(createdReservation);
        when(reservationMapper.toDTO(any(Reservation.class)))
                .thenReturn(reservationDTO);
        
        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.userId").value(1001))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(header().exists("Location"));
    }
    
    @Test
    @WithMockUser(username = "emp001", roles = "EMPLOYEE")
    @DisplayName("T051-2: 時段衝突應該回傳 409")
    void testCreateReservation_Conflict() throws Exception {
        // Given
        when(createReservationUseCase.execute(any()))
                .thenThrow(new ConflictException("會議室在此時段已被預約"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("會議室在此時段已被預約"));
    }
    
    @Test
    @WithMockUser(username = "emp001", roles = "EMPLOYEE")
    @DisplayName("T051-3: 參與者超過容量應該回傳 400")
    void testCreateReservation_ExceedsCapacity() throws Exception {
        // Given
        when(createReservationUseCase.execute(any()))
                .thenThrow(new ValidationException("參與者人數超過會議室容量"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("參與者人數超過會議室容量"));
    }
    
    @Test
    @WithMockUser(username = "emp001", roles = "EMPLOYEE")
    @DisplayName("T051-4: 缺少必填欄位應該回傳 400")
    void testCreateReservation_MissingRequiredFields() throws Exception {
        // Given
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                null, // roomId 必填欄位為 null
                Long.valueOf(1001), // userId
                "測試會議", // meetingTitle
                LocalDateTime.now(), // startTime
                LocalDateTime.now().plusHours(1), // endTime
                "", // participants
                null // recurringRule
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "emp001", roles = "EMPLOYEE")
    @DisplayName("T051-5: 結束時間早於開始時間應該回傳 400")
    void testCreateReservation_InvalidTimeRange() throws Exception {
        // Given
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                Long.valueOf(1), // roomId
                Long.valueOf(1001), // userId
                "測試會議", // meetingTitle
                LocalDateTime.of(2025, 11, 21, 10, 0), // startTime
                LocalDateTime.of(2025, 11, 21, 9, 0), // endTime (早於 startTime)
                "", // participants
                null // recurringRule
        );
        
        when(createReservationUseCase.execute(any()))
                .thenThrow(new ValidationException("結束時間必須晚於開始時間"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "emp001", roles = "EMPLOYEE")
    @DisplayName("T051-6: 創建失敗時應該正確處理")
    void testCreateReservation_Unauthorized() throws Exception {
        // Given - Use Case 返回 null (模擬某種失敗情況)
        when(createReservationUseCase.execute(any()))
                .thenReturn(null);
        
        // When & Then - 應該返回 500 (因為這是 unexpected null)
        mockMvc.perform(post("/api/v1/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isInternalServerError());
    }
    
}
