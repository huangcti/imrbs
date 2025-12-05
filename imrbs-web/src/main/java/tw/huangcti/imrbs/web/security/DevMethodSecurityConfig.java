package tw.huangcti.imrbs.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 開發環境安全配置
 * 
 * 在 dev profile 下：
 * - 禁用 @PreAuthorize, @PostAuthorize 等方法級安全檢查
 * - 允許所有 HTTP 請求 (無需認證)
 * - 禁用 CSRF 保護
 * 
 * 這讓開發者可以不需要 Keycloak/OAuth2 即可測試 API
 */
@Configuration
@Profile("dev")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
public class DevMethodSecurityConfig {
    
    /**
     * 配置 HTTP 安全策略 (開發環境 - 無認證模式)
     */
    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // CSRF 保護 (開發環境關閉)
                .csrf(AbstractHttpConfigurer::disable)
                
                // 開發環境允許所有請求 (無需認證)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                
                // Session 管理 (無狀態)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                .build();
    }
    
    /**
     * CORS 配置 (開發環境 - 寬鬆設定)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
