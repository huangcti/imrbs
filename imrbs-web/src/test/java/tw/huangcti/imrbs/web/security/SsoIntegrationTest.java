package tw.huangcti.imrbs.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SSO 單一登入整合測試
 * 
 * <p>測試場景:
 * <ol>
 *   <li>使用 Authorization Code 交換 Access Token 成功</li>
 *   <li>無效的 Authorization Code 應返回 401</li>
 *   <li>缺少 redirect_uri 參數應返回 400</li>
 *   <li>OAuth 2.0 Provider 不可用時應返回 503</li>
 * </ol>
 * 
 * <p>使用 Keycloak Testcontainers 模擬真實 OIDC Provider
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class SsoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Keycloak Testcontainer
     * 使用官方 Keycloak 映像模擬 OIDC Provider
     */
    @Container
    static GenericContainer<?> keycloak = new GenericContainer<>(
            DockerImageName.parse("quay.io/keycloak/keycloak:23.0"))
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev");

    /**
     * 動態配置 OAuth 2.0 Provider URL
     */
    @DynamicPropertySource
    static void configureOAuth2Provider(DynamicPropertyRegistry registry) {
        String issuerUri = String.format(
                "http://%s:%d/realms/imrbs",
                keycloak.getHost(),
                keycloak.getMappedPort(8080)
        );
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> issuerUri);
        registry.add("spring.security.oauth2.client.provider.keycloak.token-uri",
                () -> issuerUri + "/protocol/openid-connect/token");
        registry.add("spring.security.oauth2.client.provider.keycloak.authorization-uri",
                () -> issuerUri + "/protocol/openid-connect/auth");
        registry.add("spring.security.oauth2.client.provider.keycloak.user-info-uri",
                () -> issuerUri + "/protocol/openid-connect/userinfo");
    }

    @BeforeAll
    static void setupKeycloak() throws Exception {
        // 等待 Keycloak 啟動完成 (最多 60 秒)
        keycloak.start();
        Thread.sleep(5000); // 等待服務穩定

        // TODO: 使用 Keycloak Admin API 創建測試 Realm, Client, User
        // 範例: POST /admin/realms with realm config
        // 範例: POST /admin/realms/imrbs/clients with client config
        // 範例: POST /admin/realms/imrbs/users with test user
    }

    /**
     * 測試場景 1: 使用有效的 Authorization Code 交換 Access Token
     * 
     * Given: Keycloak 已配置測試用戶
     * When: POST /api/v1/auth/login with valid authorization code
     * Then: 返回 200 OK, 包含 access_token, expires_in
     *       設定 HttpOnly Cookie refresh_token
     */
    @Test
    void shouldExchangeAuthorizationCodeSuccessfully() throws Exception {
        // Arrange: 準備有效的 Authorization Code (需從 Keycloak 獲取)
        String validAuthCode = obtainValidAuthorizationCode(); // Helper method
        String redirectUri = "http://localhost:5173/auth/callback";

        Map<String, String> loginRequest = Map.of(
                "code", validAuthCode,
                "redirect_uri", redirectUri
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isString())
                .andExpect(jsonPath("$.access_token").value(not(emptyString())))
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andExpect(jsonPath("$.expires_in").value(900)) // 15 分鐘
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andExpect(cookie().path("refresh_token", "/api/v1/auth"));
    }

    /**
     * 測試場景 2: 使用無效的 Authorization Code 應返回 401
     * 
     * Given: Keycloak 已啟動
     * When: POST /api/v1/auth/login with invalid authorization code
     * Then: 返回 401 Unauthorized, 包含錯誤訊息
     */
    @Test
    void shouldReturn401WhenAuthorizationCodeIsInvalid() throws Exception {
        // Arrange
        Map<String, String> loginRequest = Map.of(
                "code", "invalid_auth_code_12345",
                "redirect_uri", "http://localhost:5173/auth/callback"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.message").value(containsString("Authorization Code")));
    }

    /**
     * 測試場景 3: 缺少必要參數 redirect_uri 應返回 400
     * 
     * Given: Keycloak 已啟動
     * When: POST /api/v1/auth/login without redirect_uri
     * Then: 返回 400 Bad Request, 包含驗證錯誤
     */
    @Test
    void shouldReturn400WhenRedirectUriIsMissing() throws Exception {
        // Arrange: 僅包含 code, 缺少 redirect_uri
        Map<String, String> loginRequest = Map.of("code", "some_code");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"))
                .andExpect(jsonPath("$.message").value(containsString("redirect_uri")));
    }

    /**
     * 測試場景 4: OAuth 2.0 Provider 不可用時應返回 503
     * 
     * Given: Keycloak 容器已停止
     * When: POST /api/v1/auth/login with any code
     * Then: 返回 503 Service Unavailable
     */
    @Test
    void shouldReturn503WhenOAuth2ProviderIsUnavailable() throws Exception {
        // Arrange: 停止 Keycloak 容器
        keycloak.stop();

        Map<String, String> loginRequest = Map.of(
                "code", "any_code",
                "redirect_uri", "http://localhost:5173/auth/callback"
        );

        // Act & Assert
        try {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("service_unavailable"))
                    .andExpect(jsonPath("$.message").value(containsString("OAuth 2.0 Provider")));
        } finally {
            // Cleanup: 重新啟動容器供後續測試使用
            keycloak.start();
        }
    }

    // ==================== Helper Methods ====================

    /**
     * 輔助方法: 從 Keycloak 獲取有效的 Authorization Code
     * 
     * <p>實作步驟:
     * <ol>
     *   <li>使用 Resource Owner Password Credentials 獲取 Access Token</li>
     *   <li>使用 Access Token 模擬 Authorization Code Flow</li>
     *   <li>返回 Authorization Code</li>
     * </ol>
     * 
     * @return 有效的 Authorization Code
     */
    private String obtainValidAuthorizationCode() {
        // TODO: 實作 Keycloak Authorization Code 獲取邏輯
        // 暫時返回模擬值,待 T097 OAuth2Service 實作後補齊
        return "mock_valid_auth_code_from_keycloak";
    }
}
