package tw.huangcti.imrbs.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Spring Security OAuth 2.0/OIDC 配置
 * 
 * 功能:
 * - OAuth 2.0 登入流程 (SSO 整合)
 * - JWT 驗證 (Resource Server)
 * - RBAC 權限控制
 * - 無狀態 Session (Stateless)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 配置 HTTP 安全策略
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 保護 (生產環境需啟用, 開發階段關閉)
                .csrf(AbstractHttpConfigurer::disable)
                
                // 請求授權配置
                .authorizeHttpRequests(auth -> auth
                        // 公開端點
                        .requestMatchers(
                                "/api/v1/public/**",
                                "/api/v1/guest/**",
                                "/api/v1/health",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        
                        // 管理員端點
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        
                        // 其他端點需認證
                        .anyRequest().authenticated()
                )
                
                // OAuth 2.0 登入
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
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
                
                // 自訂 JWT 過濾器 (在 UsernamePasswordAuthenticationFilter 之前)
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
}
