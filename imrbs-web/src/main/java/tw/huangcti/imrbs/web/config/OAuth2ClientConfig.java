package tw.huangcti.imrbs.web.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * OAuth 2.0 客戶端配置
 * 
 * 提供 RestTemplate bean 用於與 OIDC Provider 通信
 */
@Configuration
public class OAuth2ClientConfig {

    /**
     * 配置 RestTemplate for OAuth 2.0 Token Exchange
     * 
     * @param builder Spring Boot 自動配置的 RestTemplateBuilder
     * @return RestTemplate 實例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10)) // 連接超時 10 秒
                .setReadTimeout(Duration.ofSeconds(30))    // 讀取超時 30 秒
                .build();
    }
}
