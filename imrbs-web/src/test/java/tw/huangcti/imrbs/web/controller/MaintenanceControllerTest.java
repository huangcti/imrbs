package tw.huangcti.imrbs.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tw.huangcti.imrbs.application.usecase.CreateMaintenanceScheduleUseCase;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.web.dto.MaintenanceScheduleDTO;
import tw.huangcti.imrbs.web.mapper.MaintenanceScheduleMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T112 [P] [US4] 撰寫維護時段 API 合約測試
 * 
 * 測試場景:
 * - POST /api/v1/admin/rooms/{id}/maintenance - 新增維護時段 (需 ROOM_ADMIN 權限)
 * 
 * TDD Red Phase: 這些測試目前應該失敗，因為 Controller 端點尚未實作
 */
@WebMvcTest(controllers = {
        MaintenanceController.class,
        tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class
})
@org.springframework.context.annotation.Import(TestSecurityConfig.class)
@DisplayName("US4: 維護時段管理 API 測試")
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateMaintenanceScheduleUseCase createMaintenanceScheduleUseCase;

    @MockBean
    private MaintenanceScheduleMapper maintenanceScheduleMapper;

    private MaintenanceScheduleDTO createMaintenanceRequest;
    private MaintenanceSchedule testMaintenanceSchedule;
    private MaintenanceScheduleDTO testMaintenanceScheduleDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime startTime = LocalDateTime.of(2025, 12, 1, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 12, 1, 17, 0);

        createMaintenanceRequest = MaintenanceScheduleDTO.builder()
                .roomId(1L)
                .startTime(startTime)
                .endTime(endTime)
                .reason("設備檢修")
                .description("定期保養投影機與視訊設備")
                .build();

        testMaintenanceSchedule = MaintenanceSchedule.builder()
                .id(1L)
                .roomId(1L)
                .startTime(startTime)
                .endTime(endTime)
                .reason("設備檢修")
                .description("定期保養投影機與視訊設備")
                .status(MaintenanceSchedule.MaintenanceStatus.SCHEDULED)
                .build();

        testMaintenanceScheduleDTO = MaintenanceScheduleDTO.builder()
                .id(1L)
                .roomId(1L)
                .startTime(startTime)
                .endTime(endTime)
                .reason("設備檢修")
                .description("定期保養投影機與視訊設備")
                .status("SCHEDULED")
                .build();
    }

    /**
     * T112-1: 測試創建維護時段 (ROOM_ADMIN 權限)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("應該成功創建維護時段 (ROOM_ADMIN)")
    void testCreateMaintenance_Success() throws Exception {
        // Given
        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(1L), any(MaintenanceSchedule.class)))
                .thenReturn(testMaintenanceSchedule);
        when(maintenanceScheduleMapper.toDTO(any(MaintenanceSchedule.class)))
                .thenReturn(testMaintenanceScheduleDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.reason").value("設備檢修"))
                .andExpect(jsonPath("$.description").value("定期保養投影機與視訊設備"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    /**
     * T112-2: 測試創建維護時段 (EMPLOYEE 權限應該被拒絕)
     */
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("EMPLOYEE 角色創建維護時段應該被拒絕 (403)")
    void testCreateMaintenance_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * T112-3: 測試創建維護時段 (未登入應該被拒絕)
     */
    @Test
    @DisplayName("未登入創建維護時段應該被拒絕 (401)")
    void testCreateMaintenance_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * T112-4: 測試創建維護時段 (無效的請求資料 - 缺少必填欄位)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("創建維護時段時缺少必填欄位應該回傳 400")
    void testCreateMaintenance_InvalidRequest() throws Exception {
        // Given: 缺少必填欄位 endTime
        MaintenanceScheduleDTO invalidRequest = MaintenanceScheduleDTO.builder()
                .roomId(1L)
                .startTime(LocalDateTime.of(2025, 12, 1, 9, 0))
                .reason("設備檢修")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * T112-5: 測試創建維護時段 (結束時間早於開始時間)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("維護結束時間早於開始時間應該回傳 400")
    void testCreateMaintenance_InvalidTimeRange() throws Exception {
        // Given
        MaintenanceScheduleDTO invalidRequest = MaintenanceScheduleDTO.builder()
                .roomId(1L)
                .startTime(LocalDateTime.of(2025, 12, 1, 17, 0))
                .endTime(LocalDateTime.of(2025, 12, 1, 9, 0)) // 結束時間早於開始時間
                .reason("設備檢修")
                .build();

        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(1L), any(MaintenanceSchedule.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.ValidationException("結束時間必須晚於開始時間"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * T112-6: 測試創建維護時段 (會議室不存在)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("為不存在的會議室創建維護時段應該回傳 404")
    void testCreateMaintenance_RoomNotFound() throws Exception {
        // Given
        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(999L), any(MaintenanceSchedule.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.NotFoundException("會議室不存在"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/999/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isNotFound());
    }

    /**
     * T112-7: 測試創建維護時段 (與現有預約衝突)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("維護時段與現有預約衝突應該回傳 409")
    void testCreateMaintenance_ConflictWithReservation() throws Exception {
        // Given
        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(1L), any(MaintenanceSchedule.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.ConflictException("維護時段與現有預約衝突"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isConflict());
    }

    /**
     * T112-8: 測試創建維護時段 (與其他維護時段衝突)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("維護時段與其他維護時段衝突應該回傳 409")
    void testCreateMaintenance_ConflictWithOtherMaintenance() throws Exception {
        // Given
        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(1L), any(MaintenanceSchedule.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.ConflictException("維護時段與其他維護時段衝突"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isConflict());
    }

    /**
     * T112-9: 測試創建維護時段 (過去的時間)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("創建過去時間的維護時段應該回傳 400")
    void testCreateMaintenance_PastTime() throws Exception {
        // Given
        MaintenanceScheduleDTO pastTimeRequest = MaintenanceScheduleDTO.builder()
                .roomId(1L)
                .startTime(LocalDateTime.of(2020, 1, 1, 9, 0)) // 過去的時間
                .endTime(LocalDateTime.of(2020, 1, 1, 17, 0))
                .reason("設備檢修")
                .build();

        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(1L), any(MaintenanceSchedule.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.ValidationException("維護時段不能在過去"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastTimeRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * T112-10: 測試創建維護時段 (SYSTEM_ADMIN 也應該有權限)
     */
    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    @DisplayName("應該成功創建維護時段 (SYSTEM_ADMIN)")
    void testCreateMaintenance_SystemAdmin() throws Exception {
        // Given
        when(maintenanceScheduleMapper.toDomain(any(MaintenanceScheduleDTO.class)))
                .thenReturn(testMaintenanceSchedule);
        when(createMaintenanceScheduleUseCase.execute(eq(1L), any(MaintenanceSchedule.class)))
                .thenReturn(testMaintenanceSchedule);
        when(maintenanceScheduleMapper.toDTO(any(MaintenanceSchedule.class)))
                .thenReturn(testMaintenanceScheduleDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/rooms/1/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaintenanceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }
}
