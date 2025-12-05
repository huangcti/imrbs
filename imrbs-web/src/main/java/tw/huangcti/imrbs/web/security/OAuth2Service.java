package tw.huangcti.imrbs.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * OAuth 2.0 認證服務
 * 負責與 OIDC Provider (如 Keycloak) 進行 Authorization Code 交換
 * 
 * 功能:
 * - 使用 Authorization Code 交換 Access Token 與 Refresh Token
 * - 處理 OAuth 2.0 錯誤響應 (invalid_grant, server_error 等)
 * - 解析 ID Token 中的使用者資訊
 * 
 * OAuth 2.0 Authorization Code Flow:
 * 1. 前端重定向至 OIDC Provider 授權頁面
 * 2. 使用者登入並同意授權
 * 3. OIDC Provider 重定向回前端並附帶 Authorization Code
 * 4. 前端將 Code 傳送至後端
 * 5. 後端調用此服務交換 Code 獲取 Tokens
 * 
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    /**
     * TokenResponse - OAuth 2.0 Token Endpoint 回應
     */
    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String idToken,
            String tokenType,
            int expiresIn
    ) {}

    /**
     * 使用 Authorization Code 交換 Access Token
     * 
     * @param authorizationCode OAuth 2.0 Authorization Code
     * @param redirectUri       原始授權請求的 redirect_uri (必須完全一致)
     * @return TokenResponse 包含 access_token, refresh_token, id_token
     * @throws UnauthorizedException     當 Authorization Code 無效時
     * @throws ServiceUnavailableException 當 OIDC Provider 不可用時
     */
    public TokenResponse exchangeCode(String authorizationCode, String redirectUri) {
        log.debug("開始交換 Authorization Code: code={}, redirect_uri={}", 
                  maskSensitiveData(authorizationCode), redirectUri);

        // 構建 Token Request (application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        // Only add client_secret if it's not empty (support for public clients)
        if (clientSecret != null && !clientSecret.isBlank()) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // 發送 POST 請求至 OIDC Provider Token Endpoint
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new ServiceUnavailableException("OIDC Provider 返回空響應");
            }

            log.info("成功交換 Authorization Code,獲得 Access Token");

            return new TokenResponse(
                    (String) responseBody.get("access_token"),
                    (String) responseBody.get("refresh_token"),
                    (String) responseBody.get("id_token"),
                    (String) responseBody.getOrDefault("token_type", "Bearer"),
                    (Integer) responseBody.getOrDefault("expires_in", 900)
            );

        } catch (HttpClientErrorException.Unauthorized e) {
            // 401 Unauthorized - Authorization Code 無效或已過期
            log.warn("Authorization Code 無效: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("Authorization Code 無效或已過期");
            
        } catch (HttpClientErrorException.BadRequest e) {
            // 400 Bad Request - 參數錯誤 (如 redirect_uri 不匹配)
            String errorBody = e.getResponseBodyAsString();
            log.warn("Token 請求參數錯誤: {}", errorBody);
            
            if (errorBody.contains("invalid_grant")) {
                throw new UnauthorizedException("Authorization Code 無效或已使用");
            }
            throw new IllegalArgumentException("Token 請求參數錯誤: " + errorBody);
            
        } catch (HttpServerErrorException e) {
            // 5xx Server Error - OIDC Provider 不可用
            log.error("OIDC Provider 不可用: {}", e.getMessage());
            throw new ServiceUnavailableException("OIDC Provider 暫時不可用,請稍後再試");
            
        } catch (Exception e) {
            // 其他未預期錯誤
            log.error("交換 Authorization Code 時發生未預期錯誤", e);
            throw new RuntimeException("SSO 認證服務異常: " + e.getMessage(), e);
        }
    }

    /**
     * 遮罩敏感資料 (用於日誌)
     * 
     * @param data 原始資料
     * @return 遮罩後的資料 (保留前後各 4 個字元)
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.length() <= 8) {
            return "****";
        }
        return data.substring(0, 4) + "****" + data.substring(data.length() - 4);
    }

    /**
     * UnauthorizedException - Authorization Code 無效
     */
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    /**
     * ServiceUnavailableException - OIDC Provider 不可用
     */
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}
