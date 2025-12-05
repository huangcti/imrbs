package tw.huangcti.imrbs.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Spring Security OAuth 2.0/OIDC 配置
 * 
 * 功能:
 * - OAuth 2.0 登入流程 (SSO 整合)
 * - JWT 驗證 (Resource Server)
 * - RBAC 權限控制
 * - CSRF 保護 (生產環境)
 * - 速率限制
 * - 安全標頭
 * - 無狀態 Session (Stateless)
 * 
 * 注意: 方法級安全 (@PreAuthorize) 在 dev profile 下由 DevMethodSecurityConfig 禁用
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!dev")
public class SecurityConfig {
    
    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;
    
    public SecurityConfig(
            RateLimitingFilter rateLimitingFilter,
            SecurityHeadersFilter securityHeadersFilter
    ) {
        this.rateLimitingFilter = rateLimitingFilter;
        this.securityHeadersFilter = securityHeadersFilter;
    }
    
    /**
     * 配置 HTTP 安全策略 (生產環境)
     */
    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                // CORS 配置 (生產環境限制更嚴格)
                .cors(cors -> cors.configurationSource(prodCorsConfigurationSource()))
                
                // CSRF 保護 (生產環境啟用)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/guest/**",
                                "/actuator/**"
                        )
                )
                
                // 請求授權配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/public/**",
                                "/api/v1/guest/**",
                                "/api/v1/health",
                                "/actuator/health",
                                "/actuator/prometheus"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                
                // JWT Resource Server 配置
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                
                // Session 管理 (無狀態)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 自訂過濾器
                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, SecurityHeadersFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }
    
    /**
     * JWT 認證轉換器
     * 將 JWT claims 的 roles 轉換為 Spring Security 的 GrantedAuthority
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // 設定 roles claim 前綴為 "ROLE_"
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        // 設定從 JWT claim 中讀取 roles 的 key (預設為 "scope")
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        
        return jwtAuthenticationConverter;
    }
    
    /**
     * JWT Decoder - 用於開發測試
     * 生產環境應從 OAuth 2.0 provider 取得公鑰
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // 開發用 secret key (32 bytes for HS256)
        String secretKey = "dev-secret-key-for-testing-only-do-not-use-in-production!!";
        SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
    
    /**
     * CORS 配置 - 開發環境
     * 允許 localhost 跨域訪問
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許的來源
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        
        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允許的 Headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // 允許發送認證資訊 (Cookies, Authorization header)
        configuration.setAllowCredentials(true);
        
        // 暴露的 Headers
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
        ));
        
        // 預檢請求的快取時間 (秒)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * CORS 配置 - 生產環境
     * 限制為實際的前端域名
     */
    public CorsConfigurationSource prodCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 生產環境的前端域名 (應從環境變數讀取)
        configuration.setAllowedOrigins(Arrays.asList(
                "${FRONTEND_URL:https://imrbs.example.com}"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-XSRF-TOKEN"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
        ));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
