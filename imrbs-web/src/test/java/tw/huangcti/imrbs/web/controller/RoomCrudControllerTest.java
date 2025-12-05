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
import tw.huangcti.imrbs.application.usecase.*;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.web.dto.RoomDTO;
import tw.huangcti.imrbs.web.mapper.RoomMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T111 [P] [US4] 撰寫會議室 CRUD API 合約測試
 * 
 * 測試場景:
 * - POST /api/v1/rooms - 新增會議室 (需 ROOM_ADMIN 權限)
 * - PUT /api/v1/rooms/{id} - 更新會議室 (需 ROOM_ADMIN 權限)
 * - DELETE /api/v1/rooms/{id} - 刪除會議室 (需 ROOM_ADMIN 權限)
 * 
 * TDD Red Phase: 這些測試目前應該失敗，因為 Controller 端點尚未實作
 */
@WebMvcTest(controllers = {
        RoomController.class,
        tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class
})
@org.springframework.context.annotation.Import(TestSecurityConfig.class)
@DisplayName("US4: 會議室管理 CRUD API 測試")
class RoomCrudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateRoomUseCase createRoomUseCase;

    @MockBean
    private UpdateRoomUseCase updateRoomUseCase;

    @MockBean
    private DeleteRoomUseCase deleteRoomUseCase;

    @MockBean
    private RoomMapper roomMapper;

    private RoomDTO createRoomRequest;
    private Room testRoom;
    private RoomDTO testRoomDTO;

    @BeforeEach
    void setUp() {
        createRoomRequest = RoomDTO.builder()
                .name("會議室 C")
                .building("總部大樓")
                .floor("5F")
                .capacity(15)
                .locationDescription("電梯旁")
                .equipment(List.of("投影機", "白板", "視訊設備"))
                .features(List.of("視訊會議", "無線投影", "自然採光"))
                .build();

        testRoom = Room.builder()
                .id(3L)
                .name("會議室 C")
                .building("總部大樓")
                .floor("5F")
                .capacity(15)
                .status(Room.RoomStatus.AVAILABLE)
                .equipment(List.of(
                        Room.Equipment.builder().name("投影機").quantity(1).build(),
                        Room.Equipment.builder().name("白板").quantity(2).build()
                ))
                .features(List.of("視訊會議", "無線投影"))
                .photos(List.of())
                .build();

        testRoomDTO = RoomDTO.builder()
                .id(3L)
                .name("會議室 C")
                .building("總部大樓")
                .floor("5F")
                .capacity(15)
                .status("AVAILABLE")
                .equipment(List.of("投影機", "白板"))
                .features(List.of("視訊會議", "無線投影"))
                .build();
    }

    /**
     * T111-1: 測試創建會議室 (ROOM_ADMIN 權限)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("應該成功創建會議室 (ROOM_ADMIN)")
    void testCreateRoom_Success() throws Exception {
        // Given
        when(roomMapper.toDomain(any(RoomDTO.class))).thenReturn(testRoom);
        when(createRoomUseCase.execute(any(Room.class))).thenReturn(testRoom);
        when(roomMapper.toDTO(any(Room.class))).thenReturn(testRoomDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("會議室 C"))
                .andExpect(jsonPath("$.building").value("總部大樓"))
                .andExpect(jsonPath("$.floor").value("5F"))
                .andExpect(jsonPath("$.capacity").value(15))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    /**
     * T111-2: 測試創建會議室 (EMPLOYEE 權限應該被拒絕)
     */
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("EMPLOYEE 角色創建會議室應該被拒絕 (403)")
    void testCreateRoom_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * T111-3: 測試創建會議室 (未登入應該被拒絕)
     */
    @Test
    @DisplayName("未登入創建會議室應該被拒絕 (401)")
    void testCreateRoom_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * T111-4: 測試創建會議室 (無效的請求資料)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("創建會議室時缺少必填欄位應該回傳 400")
    void testCreateRoom_InvalidRequest() throws Exception {
        // Given: 缺少必填欄位 name
        RoomDTO invalidRequest = RoomDTO.builder()
                .building("總部大樓")
                .floor("5F")
                .capacity(15)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * T111-5: 測試更新會議室 (ROOM_ADMIN 權限)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("應該成功更新會議室 (ROOM_ADMIN)")
    void testUpdateRoom_Success() throws Exception {
        // Given
        RoomDTO updateRequest = RoomDTO.builder()
                .name("會議室 C (已更新)")
                .building("總部大樓")
                .floor("5F")
                .capacity(20) // 容量增加
                .equipment(List.of("投影機", "白板", "視訊設備", "立式白板"))
                .build();

        Room updatedRoom = testRoom.toBuilder()
                .name("會議室 C (已更新)")
                .capacity(20)
                .build();

        RoomDTO updatedRoomDTO = testRoomDTO.toBuilder()
                .name("會議室 C (已更新)")
                .capacity(20)
                .build();

        when(roomMapper.toDomain(any(RoomDTO.class))).thenReturn(updatedRoom);
        when(updateRoomUseCase.execute(eq(3L), any(Room.class))).thenReturn(updatedRoom);
        when(roomMapper.toDTO(any(Room.class))).thenReturn(updatedRoomDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/rooms/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("會議室 C (已更新)"))
                .andExpect(jsonPath("$.capacity").value(20));
    }

    /**
     * T111-6: 測試更新不存在的會議室
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("更新不存在的會議室應該回傳 404")
    void testUpdateRoom_NotFound() throws Exception {
        // Given
        when(roomMapper.toDomain(any(RoomDTO.class))).thenReturn(testRoom);
        when(updateRoomUseCase.execute(eq(999L), any(Room.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.NotFoundException("會議室不存在"));

        // When & Then
        mockMvc.perform(put("/api/v1/rooms/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isNotFound());
    }

    /**
     * T111-7: 測試更新會議室 (EMPLOYEE 權限應該被拒絕)
     */
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("EMPLOYEE 角色更新會議室應該被拒絕 (403)")
    void testUpdateRoom_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/rooms/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * T111-8: 測試刪除會議室 (ROOM_ADMIN 權限)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("應該成功刪除會議室 (ROOM_ADMIN)")
    void testDeleteRoom_Success() throws Exception {
        // Given
        doNothing().when(deleteRoomUseCase).execute(3L);

        // When & Then
        mockMvc.perform(delete("/api/v1/rooms/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    /**
     * T111-9: 測試刪除不存在的會議室
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("刪除不存在的會議室應該回傳 404")
    void testDeleteRoom_NotFound() throws Exception {
        // Given
        when(deleteRoomUseCase.execute(999L))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.NotFoundException("會議室不存在"));

        // When & Then
        mockMvc.perform(delete("/api/v1/rooms/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * T111-10: 測試刪除會議室 (EMPLOYEE 權限應該被拒絕)
     */
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("EMPLOYEE 角色刪除會議室應該被拒絕 (403)")
    void testDeleteRoom_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/rooms/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * T111-11: 測試刪除有現有預約的會議室應該失敗
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("刪除有現有預約的會議室應該回傳 409")
    void testDeleteRoom_HasReservations() throws Exception {
        // Given
        when(deleteRoomUseCase.execute(3L))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.ConflictException("會議室有現有預約，無法刪除"));

        // When & Then
        mockMvc.perform(delete("/api/v1/rooms/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    /**
     * T111-12: 測試創建會議室 (重複名稱應該失敗)
     */
    @Test
    @WithMockUser(roles = "ROOM_ADMIN")
    @DisplayName("創建重複名稱的會議室應該回傳 409")
    void testCreateRoom_DuplicateName() throws Exception {
        // Given
        when(roomMapper.toDomain(any(RoomDTO.class))).thenReturn(testRoom);
        when(createRoomUseCase.execute(any(Room.class)))
                .thenThrow(new tw.huangcti.imrbs.domain.exception.ConflictException("會議室名稱已存在"));

        // When & Then
        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isConflict());
    }
}
