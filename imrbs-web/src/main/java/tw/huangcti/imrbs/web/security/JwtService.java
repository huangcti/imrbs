package tw.huangcti.imrbs.web.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 令牌服務
 * 負責 Access Token 與 Refresh Token 的生成、驗證與解析
 * 
 * 功能:
 * - 生成 Access Token (15分鐘有效期)
 * - 生成 Refresh Token (24小時有效期)
 * - 驗證令牌簽章與有效期
 * - 提取使用者 ID 與角色資訊
 * 
 * 安全性:
 * - 使用 HS256 演算法簽章
 * - Secret 最小長度 256 bits (32 字元)
 * - 自動驗證 Secret 強度
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:900}")
    private int expiration; // 預設 15 分鐘 (900 秒)

    @Value("${jwt.refresh-expiration:86400}")
    private int refreshExpiration; // 預設 24 小時 (86400 秒)

    @Value("${jwt.issuer:imrbs-api}")
    private String issuer;

    @Value("${jwt.audience:imrbs-web}")
    private String audience;

    private SecretKey key;

    /**
     * 取得 JWT Secret (用於 SecurityConfig 建立 JwtDecoder)
     */
    public String getSecret() {
        return secret;
    }

    /**
     * 初始化 JWT 簽章金鑰並驗證 Secret 強度
     * 
     * @throws IllegalStateException 當 Secret 為空或長度不足時
     */
    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret 不可為空");
        }
        
        if (secret.length() < 32) {
            throw new IllegalStateException(
                String.format("JWT secret 長度必須至少 32 字元 (256 bits),當前長度: %d", secret.length())
            );
        }

        // 使用 UTF-8 編碼的 Secret 生成 HMAC-SHA256 金鑰
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        log.info("JWT Service 初始化完成 - Access Token 有效期: {}秒, Refresh Token 有效期: {}秒", 
                 expiration, refreshExpiration);
    }

    /**
     * 生成 Access Token
     * 
     * @param userId 使用者 ID
     * @param role   使用者角色 (EMPLOYEE, ROOM_ADMIN, SYSTEM_ADMIN)
     * @return JWT Access Token 字串
     */
    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (expiration * 1000L));

        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", role)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 生成 Refresh Token
     * 
     * @param userId 使用者 ID
     * @return JWT Refresh Token 字串
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (refreshExpiration * 1000L));

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString()) // JWT ID 用於撤銷追蹤
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 生成自訂過期時間的 Token (測試用)
     * 
     * @param userId     使用者 ID
     * @param role       使用者角色
     * @param expiration 過期時間
     * @return JWT Token 字串
     */
    public String generateTokenWithCustomExpiration(Long userId, String role, Date expiration) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", role)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 驗證 Token 簽章與有效期
     * 
     * @param token JWT Token 字串
     * @throws ExpiredJwtException     當令牌已過期
     * @throws SignatureException      當簽章驗證失敗
     * @throws MalformedJwtException   當令牌格式錯誤
     */
    public void validateToken(String token) {
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * 檢查 Token 是否有效
     * 
     * @param token JWT Token 字串
     * @return true 表示有效, false 表示無效
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 驗證失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析 Token 並返回 Claims
     * 
     * @param token JWT Token 字串
     * @return Claims 物件
     * @throws JwtException 當解析失敗時
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 從 Token 提取使用者 ID
     * 
     * @param token JWT Token 字串
     * @return 使用者 ID
     * @throws IllegalArgumentException 當 Token 為空時
     * @throws JwtException             當 Token 無效時
     */
    public Long extractUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token 不可為空");
        }
        
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 從 Token 提取使用者角色
     * 
     * @param token JWT Token 字串
     * @return 使用者角色字串
     * @throws IllegalArgumentException 當 Token 為空時
     * @throws JwtException             當 Token 無效時
     */
    public String extractRole(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token 不可為空");
        }
        
        Claims claims = parseToken(token);
        return claims.get("roles", String.class);
    }
}
