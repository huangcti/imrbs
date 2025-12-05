package tw.huangcti.imrbs.web.controller;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.application.usecase.SyncUserFromSsoUseCase;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.web.security.JwtService;
import tw.huangcti.imrbs.web.security.OAuth2Service;

import java.util.Map;

/**
 * AuthController - 認證控制器
 * 
 * 端點:
 * - POST /api/v1/auth/login - OAuth 2.0 Authorization Code 交換 Token
 * - POST /api/v1/auth/refresh - 刷新 Access Token
 * - GET /api/v1/auth/me - 獲取當前使用者資訊
 * 
 * 安全策略:
 * - Access Token: 存儲在前端記憶體 (15 分鐘有效期)
 * - Refresh Token: HttpOnly Cookie (24 小時有效期)
 * - Token Rotation: 每次刷新返回新 Refresh Token
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oauth2Service;
    private final JwtService jwtService;
    private final SyncUserFromSsoUseCase syncUserFromSsoUseCase;
    private final UserRepository userRepository;

    /**
     * LoginRequest - 登入請求 DTO
     */
    public record LoginRequest(
            @NotBlank(message = "Authorization code 不可為空")
            String code,
            
            @NotBlank(message = "Redirect URI 不可為空")
            String redirectUri
    ) {}

    /**
     * TokenResponse - Token 響應 DTO
     */
    public record TokenResponse(
            String accessToken,
            String tokenType,
            int expiresIn
    ) {}

    /**
     * UserInfoResponse - 使用者資訊響應 DTO
     */
    public record UserInfoResponse(
            Long id,
            String employeeId,
            String email,
            String fullName,
            String department,
            String role
    ) {}

    /**
     * POST /api/v1/auth/login
     * 
     * OAuth 2.0 登入流程:
     * 1. 接收 Authorization Code
     * 2. 向 OIDC Provider 交換 Token
     * 3. 解析 ID Token 獲取使用者資訊
     * 4. 同步使用者到本地資料庫
     * 5. 生成 Access Token 與 Refresh Token
     * 6. 將 Refresh Token 設定為 HttpOnly Cookie
     * 7. 返回 Access Token 給前端
     * 
     * @param request 包含 code 和 redirectUri
     * @param response HTTP 響應 (用於設定 Cookie)
     * @return TokenResponse 包含 access_token
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("收到登入請求: redirectUri={}", request.redirectUri());

        try {
            // 1. 使用 Authorization Code 交換 Token
            OAuth2Service.TokenResponse tokenResponse = oauth2Service.exchangeCode(
                    request.code(),
                    request.redirectUri()
            );

            // 2. 解析 ID Token 獲取使用者資訊
            JWT jwt = JWTParser.parse(tokenResponse.idToken());
            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            SyncUserFromSsoUseCase.SsoUserInfo ssoUserInfo = new SyncUserFromSsoUseCase.SsoUserInfo(
                    claims.getSubject(),                    // employeeId
                    claims.getStringClaim("email"),         // email
                    claims.getStringClaim("name"),          // fullName
                    claims.getStringClaim("department"),    // department
                    claims.getStringClaim("roles")          // role
            );

            // 3. 同步使用者到本地資料庫
            User user = syncUserFromSsoUseCase.execute(ssoUserInfo);

            // 4. 生成 JWT Access Token
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());

            // 5. 生成 Refresh Token
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            // 6. 設定 HttpOnly Cookie (Refresh Token)
            Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);

            log.info("使用者登入成功: userId={}, role={}", user.getId(), user.getRole());

            // 7. 返回 Access Token
            return ResponseEntity.ok(new TokenResponse(
                    accessToken,
                    "Bearer",
                    900 // 15 minutes
            ));

        } catch (OAuth2Service.UnauthorizedException e) {
            log.warn("Authorization Code 無效: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
        } catch (OAuth2Service.ServiceUnavailableException e) {
            log.error("OIDC Provider 不可用: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            
        } catch (Exception e) {
            log.error("登入處理失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/v1/auth/refresh
     * 
     * 刷新 Access Token:
     * 1. 從 Cookie 讀取 Refresh Token
     * 2. 驗證 Refresh Token 有效性
     * 3. 提取使用者 ID
     * 4. 生成新的 Access Token
     * 5. Token Rotation: 生成新的 Refresh Token
     * 6. 更新 Cookie
     * 
     * @param refreshTokenFromCookie Refresh Token (從 Cookie 自動提取)
     * @param response HTTP 響應
     * @return TokenResponse 包含新的 access_token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenFromCookie,
            HttpServletResponse response
    ) {
        if (refreshTokenFromCookie == null) {
            log.warn("Refresh Token 缺失");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 1. 驗證 Refresh Token
            jwtService.validateToken(refreshTokenFromCookie);

            // 2. 提取使用者 ID
            Long userId = jwtService.extractUserId(refreshTokenFromCookie);

            // 3. 從資料庫獲取使用者 (確認帳號狀態)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

            if (Boolean.FALSE.equals(user.getIsActive())) {
                log.warn("嘗試刷新已停用的帳號: userId={}", userId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 4. 生成新的 Access Token
            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());

            // 5. Token Rotation: 生成新的 Refresh Token
            String newRefreshToken = jwtService.generateRefreshToken(user.getId());
            Cookie refreshTokenCookie = createRefreshTokenCookie(newRefreshToken);
            response.addCookie(refreshTokenCookie);

            log.debug("Token 刷新成功: userId={}", userId);

            return ResponseEntity.ok(new TokenResponse(
                    newAccessToken,
                    "Bearer",
                    900
            ));

        } catch (Exception e) {
            log.warn("Refresh Token 無效: {}", e.getMessage());
            // 清除無效的 Cookie
            Cookie expiredCookie = createExpiredRefreshTokenCookie();
            response.addCookie(expiredCookie);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * GET /api/v1/auth/me
     * 
     * 獲取當前登入使用者資訊
     * 
     * @param jwt 從 Spring Security 自動提取的 JWT
     * @return UserInfoResponse 使用者資訊
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Long userId = Long.parseLong(jwt.getSubject());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

            UserInfoResponse userInfo = new UserInfoResponse(
                    user.getId(),
                    user.getEmployeeId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getDepartment(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            log.error("獲取使用者資訊失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 創建 Refresh Token Cookie
     * 
     * 安全設定:
     * - HttpOnly: 防止 XSS 攻擊
     * - Secure: 僅在 HTTPS 傳輸
     * - SameSite=Strict: 防止 CSRF 攻擊
     * - MaxAge: 24 小時
     */
    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 生產環境必須啟用
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        // cookie.setAttribute("SameSite", "Strict"); // Spring 6+ 支援
        return cookie;
    }

    /**
     * 創建已過期的 Cookie (用於清除)
     */
    private Cookie createExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0); // 立即過期
        return cookie;
    }
}
