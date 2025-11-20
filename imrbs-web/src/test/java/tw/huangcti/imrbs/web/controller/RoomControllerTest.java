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
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.service.RoomAvailabilityService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoomControllerTest - 會議室 API 合約測試 (TDD)
 * 
 * 測試場景:
 * - GET /api/v1/rooms - 查詢所有會議室
 * - GET /api/v1/rooms/{id} - 查詢單一會議室詳情
 * - GET /api/v1/rooms/{id}/availability - 查詢會議室可用時段
 */
@WebMvcTest(controllers = {
        RoomController.class,
        tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class
})
@org.springframework.context.annotation.Import(TestSecurityConfig.class)
@DisplayName("US1: 會議室查詢 API 測試")
class RoomControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RoomAvailabilityService roomAvailabilityService;
    
    @MockBean
    private tw.huangcti.imrbs.web.mapper.RoomMapper roomMapper;
    
    private Room testRoom;
    
    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .name("會議室 A")
                .building("總部大樓")
                .floor("3F")
                .capacity(10)
                .status(Room.RoomStatus.AVAILABLE)
                .equipment(List.of(
                        Room.Equipment.builder().name("投影機").quantity(1).build(),
                        Room.Equipment.builder().name("白板").quantity(2).build()
                ))
                .features(List.of("視訊會議", "無線投影"))
                .photos(List.of("/images/room-a-1.jpg"))
                .build();
    }
    
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("T050-1: 應該成功查詢所有可用會議室")
    void testGetAvailableRooms_Success() throws Exception {
        // Given
        when(roomAvailabilityService.findAvailableRooms(any(), any(), anyInt()))
                .thenReturn(List.of(testRoom));
        
        tw.huangcti.imrbs.web.dto.RoomDTO roomDTO = tw.huangcti.imrbs.web.dto.RoomDTO.builder()
                .id(1L)
                .name("會議室 A")
                .capacity(10)
                .status("AVAILABLE")
                .build();
        when(roomMapper.toDTO(any(Room.class)))
                .thenReturn(roomDTO);
        
        // When & Then
        mockMvc.perform(get("/api/v1/rooms")
                        .param("startTime", "2025-11-21T09:00:00")
                        .param("endTime", "2025-11-21T10:00:00")
                        .param("capacity", "8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("會議室 A"))
                .andExpect(jsonPath("$[0].capacity").value(10))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }
    
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("T050-2: 應該成功查詢單一會議室詳情")
    void testGetRoomById_Success() throws Exception {
        // Given
        when(roomAvailabilityService.findRoomById(1L))
                .thenReturn(Optional.of(testRoom));
        
        // When & Then
        mockMvc.perform(get("/api/v1/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("會議室 A"))
                .andExpect(jsonPath("$.building").value("總部大樓"))
                .andExpect(jsonPath("$.floor").value("3F"))
                .andExpect(jsonPath("$.equipment[0].name").value("投影機"))
                .andExpect(jsonPath("$.features[0]").value("視訊會議"));
    }
    
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("T050-3: 查詢不存在的會議室應該回傳 404")
    void testGetRoomById_NotFound() throws Exception {
        // Given
        when(roomAvailabilityService.findRoomById(999L))
                .thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/v1/rooms/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("T050-4: 應該成功查詢會議室可用時段")
    void testGetRoomAvailability_Success() throws Exception {
        // Given
        LocalDateTime date = LocalDateTime.of(2025, 11, 21, 0, 0);
        List<RoomAvailabilityService.TimeSlot> availableSlots = List.of(
                new RoomAvailabilityService.TimeSlot(
                        LocalDateTime.of(2025, 11, 21, 9, 0),
                        LocalDateTime.of(2025, 11, 21, 10, 0),
                        true
                ),
                new RoomAvailabilityService.TimeSlot(
                        LocalDateTime.of(2025, 11, 21, 14, 0),
                        LocalDateTime.of(2025, 11, 21, 16, 0),
                        true
                )
        );
        
        when(roomAvailabilityService.getAvailableTimeSlots(eq(1L), any()))
                .thenReturn(availableSlots);
        
        // When & Then
        mockMvc.perform(get("/api/v1/rooms/1/availability")
                        .param("date", "2025-11-21")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").exists())
                .andExpect(jsonPath("$[0].endTime").exists())
                .andExpect(jsonPath("$[0].available").value(true));
    }
    
    @Test
    @DisplayName("T050-5: 未認證使用者應該被拒絕存取")
    void testGetAvailableRooms_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("T050-6: 缺少必要參數應該回傳 400")
    void testGetAvailableRooms_MissingParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/rooms")
                        .param("startTime", "2025-11-21T09:00:00")
                        // 缺少 endTime
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
