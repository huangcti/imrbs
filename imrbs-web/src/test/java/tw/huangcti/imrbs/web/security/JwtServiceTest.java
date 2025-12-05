package tw.huangcti.imrbs.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT Token 生成與驗證服務單元測試
 * 
 * <p>測試場景:
 * <ol>
 *   <li>生成 Access Token 包含正確的 claims (sub, iat, exp, roles)</li>
 *   <li>生成 Refresh Token 包含正確的 jti 與 type</li>
 *   <li>驗證有效的 JWT Token 成功</li>
 *   <li>驗證過期的 JWT Token 拋出 ExpiredJwtException</li>
 *   <li>驗證簽名錯誤的 Token 拋出 SignatureException</li>
 *   <li>驗證格式錯誤的 Token 拋出 MalformedJwtException</li>
 *   <li>從 Token 提取 userId 成功</li>
 *   <li>從 Token 提取 roles 成功</li>
 * </ol>
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */
@DisplayName("JwtService 單元測試")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "test_jwt_secret_at_least_32_characters_long_for_hs256_algorithm";
    private static final Long TEST_USER_ID = 1001L;
    private static final String TEST_ROLE = "EMPLOYEE";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 使用反射注入測試 Secret (避免依賴 Spring 容器)
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 900); // 15 分鐘
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400); // 24 小時
        ReflectionTestUtils.setField(jwtService, "issuer", "imrbs-api-test");
        ReflectionTestUtils.setField(jwtService, "audience", "imrbs-web-test");
        
        // 手動調用 @PostConstruct 方法初始化 SecretKey
        jwtService.validateSecret();
    }

    /**
     * 測試場景 1: 生成 Access Token 包含正確的 claims
     * 
     * Given: userId = 1001, role = EMPLOYEE
     * When: generateAccessToken(userId, role)
     * Then: Token 包含 sub=1001, roles=[EMPLOYEE], iat, exp, iss, aud
     */
    @Test
    @DisplayName("應生成包含正確 claims 的 Access Token")
    void shouldGenerateAccessTokenWithCorrectClaims() {
        // Act
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(TEST_USER_ID.toString());
        assertThat(claims.get("roles", String.class)).isEqualTo(TEST_ROLE);
        assertThat(claims.getIssuer()).isEqualTo("imrbs-api-test");
        assertThat(claims.getAudience()).containsExactly("imrbs-web-test");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();

        // 驗證過期時間約為 15 分鐘後
        long expirationTime = claims.getExpiration().getTime();
        long issuedTime = claims.getIssuedAt().getTime();
        long diffSeconds = (expirationTime - issuedTime) / 1000;
        assertThat(diffSeconds).isBetween(895L, 905L); // 允許 5 秒誤差
    }

    /**
     * 測試場景 2: 生成 Refresh Token 包含正確的 jti 與 type
     * 
     * Given: userId = 1001
     * When: generateRefreshToken(userId)
     * Then: Token 包含 sub=1001, jti (UUID), type=refresh, 過期時間 24 小時
     */
    @Test
    @DisplayName("應生成包含 jti 與 type 的 Refresh Token")
    void shouldGenerateRefreshTokenWithJtiAndType() {
        // Act
        String token = jwtService.generateRefreshToken(TEST_USER_ID);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(TEST_USER_ID.toString());
        assertThat(claims.getId()).isNotNull(); // jti
        assertThat(claims.getId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"); // UUID 格式
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");

        // 驗證過期時間約為 24 小時後
        long expirationTime = claims.getExpiration().getTime();
        long issuedTime = claims.getIssuedAt().getTime();
        long diffHours = (expirationTime - issuedTime) / (1000 * 60 * 60);
        assertThat(diffHours).isBetween(23L, 25L); // 允許 1 小時誤差
    }

    /**
     * 測試場景 3: 驗證有效的 JWT Token 成功
     * 
     * Given: 有效的 JWT Token
     * When: validateToken(token)
     * Then: 不拋出異常, 返回 true
     */
    @Test
    @DisplayName("應成功驗證有效的 JWT Token")
    void shouldValidateValidTokenSuccessfully() {
        // Arrange
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        // Act & Assert
        assertThatCode(() -> jwtService.validateToken(token))
                .doesNotThrowAnyException();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    /**
     * 測試場景 4: 驗證過期的 JWT Token 拋出 ExpiredJwtException
     * 
     * Given: 已過期的 JWT Token (expiration = -1 秒)
     * When: validateToken(expiredToken)
     * Then: 拋出 ExpiredJwtException
     */
    @Test
    @DisplayName("應拋出 ExpiredJwtException 當 Token 已過期")
    void shouldThrowExpiredJwtExceptionWhenTokenIsExpired() {
        // Arrange: 生成一個已過期的 Token
        String expiredToken = jwtService.generateTokenWithCustomExpiration(
                TEST_USER_ID,
                TEST_ROLE,
                Date.from(LocalDateTime.now().minusSeconds(1)
                        .atZone(ZoneId.systemDefault()).toInstant())
        );

        // Act & Assert
        assertThatThrownBy(() -> jwtService.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("expired");
        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    /**
     * 測試場景 5: 驗證簽名錯誤的 Token 拋出 SignatureException
     * 
     * Given: 使用錯誤 Secret 簽名的 Token
     * When: validateToken(tokenWithWrongSignature)
     * Then: 拋出 SignatureException
     */
    @Test
    @DisplayName("應拋出 SignatureException 當 Token 簽名錯誤")
    void shouldThrowSignatureExceptionWhenTokenSignatureIsInvalid() {
        // Arrange: 使用不同 Secret 生成 Token
        JwtService wrongSecretService = new JwtService();
        ReflectionTestUtils.setField(wrongSecretService, "secret", "wrong_secret_different_from_test_secret_12345678");
        ReflectionTestUtils.setField(wrongSecretService, "expiration", 900);
        ReflectionTestUtils.setField(wrongSecretService, "issuer", "imrbs-api-test");
        ReflectionTestUtils.setField(wrongSecretService, "audience", "imrbs-web-test");
        wrongSecretService.validateSecret(); // 初始化 SecretKey

        String tokenWithWrongSignature = wrongSecretService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.validateToken(tokenWithWrongSignature))
                .isInstanceOf(SignatureException.class)
                .hasMessageContaining("signature");
        assertThat(jwtService.isTokenValid(tokenWithWrongSignature)).isFalse();
    }

    /**
     * 測試場景 6: 驗證格式錯誤的 Token 拋出 MalformedJwtException
     * 
     * Given: 格式不符合 JWT 標準的字串
     * When: validateToken(malformedToken)
     * Then: 拋出 MalformedJwtException
     */
    @Test
    @DisplayName("應拋出 MalformedJwtException 當 Token 格式錯誤")
    void shouldThrowMalformedJwtExceptionWhenTokenFormatIsInvalid() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // Act & Assert
        assertThatThrownBy(() -> jwtService.validateToken(malformedToken))
                .isInstanceOf(MalformedJwtException.class)
                .hasMessageContaining("Invalid");
        assertThat(jwtService.isTokenValid(malformedToken)).isFalse();
    }

    /**
     * 測試場景 7: 從 Token 提取 userId 成功
     * 
     * Given: 有效的 JWT Token
     * When: extractUserId(token)
     * Then: 返回正確的 userId (1001)
     */
    @Test
    @DisplayName("應從 Token 成功提取 userId")
    void shouldExtractUserIdFromToken() {
        // Arrange
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        // Act
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertThat(extractedUserId).isEqualTo(TEST_USER_ID);
    }

    /**
     * 測試場景 8: 從 Token 提取 roles 成功
     * 
     * Given: 包含 roles claim 的 JWT Token
     * When: extractRoles(token)
     * Then: 返回正確的 role (EMPLOYEE)
     */
    @Test
    @DisplayName("應從 Token 成功提取 roles")
    void shouldExtractRolesFromToken() {
        // Arrange
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        // Act
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo(TEST_ROLE);
    }

    /**
     * 測試場景 9: 提取空 Token 應拋出異常
     * 
     * Given: token = null 或 empty
     * When: extractUserId(token)
     * Then: 拋出 IllegalArgumentException
     */
    @Test
    @DisplayName("應拋出 IllegalArgumentException 當提取空 Token")
    void shouldThrowIllegalArgumentExceptionWhenExtractingNullToken() {
        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token 不可為空");

        assertThatThrownBy(() -> jwtService.extractUserId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token 不可為空");
    }

    /**
     * 測試場景 10: validateSecret() 應在 Secret 長度不足時拋出異常
     * 
     * Given: JWT Secret 長度 < 32 字元
     * When: @PostConstruct validateSecret()
     * Then: 拋出 IllegalStateException
     */
    @Test
    @DisplayName("應在 Secret 長度不足時拋出異常")
    void shouldThrowExceptionWhenSecretLengthIsInsufficient() {
        // Arrange
        JwtService invalidSecretService = new JwtService();
        ReflectionTestUtils.setField(invalidSecretService, "secret", "short_secret");

        // Act & Assert
        assertThatThrownBy(invalidSecretService::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 字元");
    }

    /**
     * 測試場景 11: validateSecret() 應在 Secret 為 null 時拋出異常
     * 
     * Given: JWT Secret = null
     * When: @PostConstruct validateSecret()
     * Then: 拋出 IllegalStateException
     */
    @Test
    @DisplayName("應在 Secret 為 null 時拋出異常")
    void shouldThrowExceptionWhenSecretIsNull() {
        // Arrange
        JwtService nullSecretService = new JwtService();
        ReflectionTestUtils.setField(nullSecretService, "secret", null);

        // Act & Assert
        assertThatThrownBy(nullSecretService::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret");
    }
}
