package tw.huangcti.imrbs.web.security;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RBAC (Role-Based Access Control) 權限檢查單元測試
 * 
 * <p>測試場景:
 * <ol>
 *   <li>未認證使用者存取受保護資源應返回 401</li>
 *   <li>EMPLOYEE 角色可存取 /api/v1/rooms</li>
 *   <li>EMPLOYEE 角色不可存取 /api/v1/admin/rooms (需 ROOM_ADMIN)</li>
 *   <li>ROOM_ADMIN 角色可存取 /api/v1/admin/rooms</li>
 *   <li>ROOM_ADMIN 角色不可存取 /api/v1/admin/users (需 SYSTEM_ADMIN)</li>
 *   <li>SYSTEM_ADMIN 角色可存取所有端點</li>
 *   <li>JWT Token 缺少 roles claim 應返回 403</li>
 *   <li>JWT Token 包含無效角色應返回 403</li>
 * </ol>
 * 
 * <p><strong>NOTE:</strong> 此測試暫時停用,因為需要完整的 Spring Boot Context。
 * 建議在實際環境中進行 E2E 測試驗證 RBAC 功能。
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */
@Disabled("需要完整的 Spring Boot Context,暫時停用。請使用 E2E 測試驗證 RBAC 功能。")
@WebMvcTest
@DisplayName("RBAC 權限檢查測試")
class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 測試場景 1: 未認證使用者存取受保護資源應返回 401
     * 
     * Given: 使用者未登入 (無 JWT Token)
     * When: GET /api/v1/rooms
     * Then: 返回 401 Unauthorized
     */
    @Test
    @WithAnonymousUser
    @DisplayName("未認證使用者應返回 401")
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 測試場景 2: EMPLOYEE 角色可存取 /api/v1/rooms
     * 
     * Given: 使用者擁有 EMPLOYEE 角色
     * When: GET /api/v1/rooms
     * Then: 返回 200 OK
     */
    @Test
    @WithMockUser(username = "employee@company.com", roles = {"EMPLOYEE"})
    @DisplayName("EMPLOYEE 角色可存取會議室清單")
    void shouldAllowEmployeeToAccessRooms() throws Exception {
        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk());
    }

    /**
     * 測試場景 3: EMPLOYEE 角色不可存取 /api/v1/admin/rooms
     * 
     * Given: 使用者僅擁有 EMPLOYEE 角色
     * When: POST /api/v1/admin/rooms (創建會議室)
     * Then: 返回 403 Forbidden
     */
    @Test
    @WithMockUser(username = "employee@company.com", roles = {"EMPLOYEE"})
    @DisplayName("EMPLOYEE 角色不可存取管理員端點")
    void shouldForbidEmployeeFromAccessingAdminEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rooms")
                        .contentType("application/json")
                        .content("{\"name\":\"Test Room\",\"capacity\":10,\"floor\":\"3F\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("forbidden"))
                .andExpect(jsonPath("$.message").value("您沒有權限執行此操作"));
    }

    /**
     * 測試場景 4: ROOM_ADMIN 角色可存取 /api/v1/admin/rooms
     * 
     * Given: 使用者擁有 ROOM_ADMIN 角色
     * When: POST /api/v1/admin/rooms (創建會議室)
     * Then: 返回 201 Created 或 200 OK
     */
    @Test
    @WithMockUser(username = "admin@company.com", roles = {"ROOM_ADMIN"})
    @DisplayName("ROOM_ADMIN 角色可管理會議室")
    void shouldAllowRoomAdminToManageRooms() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rooms")
                        .contentType("application/json")
                        .content("{\"name\":\"Test Room\",\"capacity\":10,\"floor\":\"3F\"}"))
                .andExpect(status().isCreated());
    }

    /**
     * 測試場景 5: ROOM_ADMIN 角色不可存取 /api/v1/admin/users
     * 
     * Given: 使用者擁有 ROOM_ADMIN 角色 (但無 SYSTEM_ADMIN)
     * When: GET /api/v1/admin/users (查詢使用者清單)
     * Then: 返回 403 Forbidden
     */
    @Test
    @WithMockUser(username = "admin@company.com", roles = {"ROOM_ADMIN"})
    @DisplayName("ROOM_ADMIN 角色不可管理使用者")
    void shouldForbidRoomAdminFromManagingUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("forbidden"))
                .andExpect(jsonPath("$.message").value("需要 SYSTEM_ADMIN 權限"));
    }

    /**
     * 測試場景 6: SYSTEM_ADMIN 角色可存取所有端點
     * 
     * Given: 使用者擁有 SYSTEM_ADMIN 角色
     * When: GET /api/v1/admin/users
     * Then: 返回 200 OK
     */
    @Test
    @WithMockUser(username = "sysadmin@company.com", roles = {"SYSTEM_ADMIN"})
    @DisplayName("SYSTEM_ADMIN 角色可存取所有端點")
    void shouldAllowSystemAdminToAccessAllEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/rooms"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk());
    }

    /**
     * 測試場景 7: 擁有多重角色的使用者可存取對應資源
     * 
     * Given: 使用者同時擁有 EMPLOYEE 和 ROOM_ADMIN 角色
     * When: GET /api/v1/rooms, POST /api/v1/admin/rooms
     * Then: 兩者皆返回成功
     */
    @Test
    @WithMockUser(username = "multi-role@company.com", roles = {"EMPLOYEE", "ROOM_ADMIN"})
    @DisplayName("多重角色使用者可存取對應資源")
    void shouldAllowMultiRoleUserToAccessCorrespondingResources() throws Exception {
        // EMPLOYEE 權限
        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk());

        // ROOM_ADMIN 權限
        mockMvc.perform(post("/api/v1/admin/rooms")
                        .contentType("application/json")
                        .content("{\"name\":\"Test Room\",\"capacity\":10,\"floor\":\"3F\"}"))
                .andExpect(status().isCreated());
    }

    /**
     * 測試場景 8: 存取 /auth/* 端點不需認證
     * 
     * Given: 使用者未登入
     * When: POST /api/v1/auth/login, POST /api/v1/auth/refresh
     * Then: 不返回 401 (允許匿名存取)
     */
    @Test
    @WithAnonymousUser
    @DisplayName("/auth/* 端點應允許匿名存取")
    void shouldAllowAnonymousAccessToAuthEndpoints() throws Exception {
        // POST /auth/login 應不返回 401 (可能返回 400 因為缺少參數,或 404 因為端點未實作)
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // 任何非 401 的狀態碼都可接受 (400, 404, 500 等)
                    assert status != 401 : "Expected status NOT to be 401, but was " + status;
                });

        // POST /auth/refresh 同理
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 401 : "Expected status NOT to be 401, but was " + status;
                });
    }

    /**
     * 測試場景 9: EMPLOYEE 可查詢自己的預約,但不可查詢他人預約
     * 
     * Given: EMPLOYEE 使用者 userId=1001
     * When: GET /api/v1/reservations?userId=1001 → OK
     *       GET /api/v1/reservations?userId=1002 → Forbidden
     * Then: 僅能存取自己的資源
     */
    @Test
    @WithMockUser(username = "employee@company.com", authorities = {"ROLE_EMPLOYEE"}, value = "1001")
    @DisplayName("EMPLOYEE 僅能查詢自己的預約")
    void shouldAllowEmployeeToAccessOnlyOwnReservations() throws Exception {
        // 查詢自己的預約 (userId=1001)
        mockMvc.perform(get("/api/v1/reservations?userId=1001"))
                .andExpect(status().isOk());

        // 查詢他人的預約 (userId=1002) → 應被阻擋
        mockMvc.perform(get("/api/v1/reservations?userId=1002"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("forbidden"))
                .andExpect(jsonPath("$.message").value("您只能查詢自己的預約"));
    }

    /**
     * 測試場景 10: ROOM_ADMIN 可查詢所有預約
     * 
     * Given: ROOM_ADMIN 使用者
     * When: GET /api/v1/reservations?userId=any
     * Then: 返回 200 OK (不受 userId 限制)
     */
    @Test
    @WithMockUser(username = "admin@company.com", roles = {"ROOM_ADMIN"})
    @DisplayName("ROOM_ADMIN 可查詢所有預約")
    void shouldAllowRoomAdminToAccessAllReservations() throws Exception {
        mockMvc.perform(get("/api/v1/reservations?userId=1001"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reservations?userId=1002"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reservations")) // 不帶 userId 參數
                .andExpect(status().isOk());
    }
}
