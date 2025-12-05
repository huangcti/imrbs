package tw.huangcti.imrbs.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import tw.huangcti.imrbs.application.usecase.UpdateLanguagePreferenceUseCase;
import tw.huangcti.imrbs.application.usecase.GetUserProfileUseCase;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T163 [P] [US8] 語言切換 API 合約測試
 * 
 * 測試端點: PUT /api/users/me/language
 * 
 * 測試場景:
 * 1. 切換語言為繁體中文 (zh-TW) - 成功
 * 2. 切換語言為英文 (en) - 成功
 * 3. 切換語言 - 無效語言代碼返回 400
 * 4. 切換語言 - 未登入返回 401
 * 5. 取得目前使用者語言偏好 - 成功
 * 6. 切換語言 - 使用者不存在返回 404
 * 7. 取得使用者個人資料包含語言偏好 - 成功
 * 8. 切換語言後驗證資料已更新
 */
@WebMvcTest(
        controllers = {
                UserController.class,
                tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class
        },
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        UserController.class,
                        tw.huangcti.imrbs.web.exception.GlobalExceptionHandler.class
                }
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@Import(TestSecurityConfig.class)
@DisplayName("US8: 語言切換 API 測試")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpdateLanguagePreferenceUseCase updateLanguagePreferenceUseCase;

    @MockBean
    private GetUserProfileUseCase getUserProfileUseCase;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .employeeId("E001")
                .email("test@example.com")
                .fullName("測試使用者")
                .department("IT 部門")
                .role(User.UserRole.EMPLOYEE)
                .languagePreference("zh-TW")
                .phoneNumber("0912345678")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("PUT /api/users/me/language - 更新語言偏好")
    class UpdateLanguagePreferenceTests {

        @Test
        @WithMockUser(username = "E001", roles = {"EMPLOYEE"})
        @DisplayName("1. 切換語言為繁體中文 (zh-TW) - 成功")
        void testUpdateLanguagePreference_ToTraditionalChinese_Success() throws Exception {
            // Given
            UpdateLanguageRequest request = new UpdateLanguageRequest("zh-TW");
            User updatedUser = User.builder()
                    .id(1L)
                    .employeeId("E001")
                    .email("test@example.com")
                    .fullName("測試使用者")
                    .languagePreference("zh-TW")
                    .build();

            when(updateLanguagePreferenceUseCase.execute(eq("E001"), eq("zh-TW")))
                    .thenReturn(updatedUser);

            // When & Then
            mockMvc.perform(put("/api/users/me/language")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.languagePreference").value("zh-TW"))
                    .andExpect(jsonPath("$.message").value("語言偏好已更新"));

            verify(updateLanguagePreferenceUseCase, times(1)).execute("E001", "zh-TW");
        }

        @Test
        @WithMockUser(username = "E001", roles = {"EMPLOYEE"})
        @DisplayName("2. 切換語言為英文 (en) - 成功")
        void testUpdateLanguagePreference_ToEnglish_Success() throws Exception {
            // Given
            UpdateLanguageRequest request = new UpdateLanguageRequest("en");
            User updatedUser = User.builder()
                    .id(1L)
                    .employeeId("E001")
                    .email("test@example.com")
                    .fullName("Test User")
                    .languagePreference("en")
                    .build();

            when(updateLanguagePreferenceUseCase.execute(eq("E001"), eq("en")))
                    .thenReturn(updatedUser);

            // When & Then
            mockMvc.perform(put("/api/users/me/language")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.languagePreference").value("en"))
                    .andExpect(jsonPath("$.message").value("Language preference updated"));

            verify(updateLanguagePreferenceUseCase, times(1)).execute("E001", "en");
        }

        @Test
        @WithMockUser(username = "E001", roles = {"EMPLOYEE"})
        @DisplayName("3. 切換語言 - 無效語言代碼返回 400")
        void testUpdateLanguagePreference_InvalidLanguageCode_Returns400() throws Exception {
            // Given
            UpdateLanguageRequest request = new UpdateLanguageRequest("invalid-lang");

            when(updateLanguagePreferenceUseCase.execute(eq("E001"), eq("invalid-lang")))
                    .thenThrow(new ValidationException("不支援的語言: invalid-lang"));

            // When & Then
            mockMvc.perform(put("/api/users/me/language")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("4. 切換語言 - 未登入返回 401")
        void testUpdateLanguagePreference_Unauthorized_Returns401() throws Exception {
            // Given
            UpdateLanguageRequest request = new UpdateLanguageRequest("zh-TW");

            // When & Then
            mockMvc.perform(put("/api/users/me/language")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "E999", roles = {"EMPLOYEE"})
        @DisplayName("6. 切換語言 - 使用者不存在返回 404")
        void testUpdateLanguagePreference_UserNotFound_Returns404() throws Exception {
            // Given
            UpdateLanguageRequest request = new UpdateLanguageRequest("zh-TW");

            when(updateLanguagePreferenceUseCase.execute(eq("E999"), eq("zh-TW")))
                    .thenThrow(new NotFoundException("找不到使用者: E999"));

            // When & Then
            mockMvc.perform(put("/api/users/me/language")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser(username = "E001", roles = {"EMPLOYEE"})
        @DisplayName("8. 切換語言後驗證資料已更新")
        void testUpdateLanguagePreference_VerifyDataUpdated() throws Exception {
            // Given
            UpdateLanguageRequest request = new UpdateLanguageRequest("en");
            LocalDateTime now = LocalDateTime.now();
            User updatedUser = User.builder()
                    .id(1L)
                    .employeeId("E001")
                    .email("test@example.com")
                    .fullName("Test User")
                    .languagePreference("en")
                    .updatedAt(now)
                    .build();

            when(updateLanguagePreferenceUseCase.execute(eq("E001"), eq("en")))
                    .thenReturn(updatedUser);

            // When & Then
            mockMvc.perform(put("/api/users/me/language")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.languagePreference").value("en"))
                    .andExpect(jsonPath("$.data.updatedAt").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 取得個人資料")
    class GetUserProfileTests {

        @Test
        @WithMockUser(username = "E001", roles = {"EMPLOYEE"})
        @DisplayName("5. 取得目前使用者語言偏好 - 成功")
        void testGetUserProfile_WithLanguagePreference_Success() throws Exception {
            // Given
            when(getUserProfileUseCase.execute(eq("E001")))
                    .thenReturn(testUser);

            // When & Then
            mockMvc.perform(get("/api/users/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.employeeId").value("E001"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.fullName").value("測試使用者"))
                    .andExpect(jsonPath("$.data.languagePreference").value("zh-TW"));
        }

        @Test
        @WithMockUser(username = "E001", roles = {"EMPLOYEE"})
        @DisplayName("7. 取得使用者個人資料包含語言偏好 - 成功")
        void testGetUserProfile_ContainsAllFields_Success() throws Exception {
            // Given
            when(getUserProfileUseCase.execute(eq("E001")))
                    .thenReturn(testUser);

            // When & Then
            mockMvc.perform(get("/api/users/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.employeeId").value("E001"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.fullName").value("測試使用者"))
                    .andExpect(jsonPath("$.data.department").value("IT 部門"))
                    .andExpect(jsonPath("$.data.role").value("EMPLOYEE"))
                    .andExpect(jsonPath("$.data.languagePreference").value("zh-TW"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("0912345678"))
                    .andExpect(jsonPath("$.data.isActive").value(true));
        }

        @Test
        @DisplayName("取得個人資料 - 未登入返回 401")
        void testGetUserProfile_Unauthorized_Returns401() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/me")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 更新語言請求 DTO (內部類別)
     */
    static class UpdateLanguageRequest {
        private String language;

        public UpdateLanguageRequest() {}

        public UpdateLanguageRequest(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }
}
