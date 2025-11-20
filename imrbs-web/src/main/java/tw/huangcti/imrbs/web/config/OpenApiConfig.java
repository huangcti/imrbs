package tw.huangcti.imrbs.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig - SpringDoc OpenAPI 配置
 * 
 * 功能:
 * - API 文檔生成
 * - Swagger UI 介面
 * - JWT Bearer Token 認證配置
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${app.name:IMRBS}")
    private String appName;
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.description:會議室預約系統}")
    private String appDescription;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }
    
    /**
     * API 基本資訊
     */
    private Info apiInfo() {
        return new Info()
                .title(appName + " API")
                .version(appVersion)
                .description(appDescription + " - REST API 文檔")
                .contact(new Contact()
                        .name("IMRBS Development Team")
                        .email("support@example.com")
                        .url("https://github.com/huangcti/imrbs"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
    
    /**
     * API 伺服器列表
     */
    private List<Server> apiServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("本地開發環境"),
                new Server()
                        .url("https://dev.imrbs.example.com")
                        .description("開發測試環境"),
                new Server()
                        .url("https://imrbs.example.com")
                        .description("正式生產環境")
        );
    }
    
    /**
     * 安全組件 (JWT Bearer Token)
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", 
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("輸入 JWT Token (登入後從 SSO 取得)")
                );
    }
    
    /**
     * 安全需求 (所有端點預設需要 JWT)
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }
}
