package tw.huangcti.imrbs.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtAuthenticationFilter - JWT 認證過濾器
 * 
 * 功能:
 * - 從 Authorization Header 提取 JWT Token
 * - 驗證 JWT 有效性 (簽章、過期時間)
 * - 將使用者資訊載入 SecurityContext
 * - 處理 JWT 驗證失敗的情況
 * 
 * 注意: 此過濾器在 dev profile 下被禁用
 */
@Slf4j
@Component
@Profile("!dev")
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtDecoder jwtDecoder;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 提取 JWT Token
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 解碼並驗證 JWT
                Jwt decodedJwt = jwtDecoder.decode(jwt);
                
                // 提取使用者資訊
                String username = decodedJwt.getSubject();
                
                // 提取角色資訊
                @SuppressWarnings("unchecked")
                List<String> roles = decodedJwt.getClaimAsStringList("roles");
                if (roles == null) {
                    roles = List.of("EMPLOYEE"); // 預設角色
                }
                
                // 轉換為 GrantedAuthority
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                
                // 建立認證物件
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 將認證資訊載入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set Authentication: {} with roles: {}", username, roles);
            }
        } catch (JwtException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            // 不中斷請求流程, 讓 Spring Security 處理未認證的情況
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 從 HTTP Request 提取 JWT Token
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}
