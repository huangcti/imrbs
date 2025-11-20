package tw.huangcti.imrbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IMRBS - Integrated Meeting Room Booking System
 * 會議室預約系統主應用程式
 * 
 * <p>採用 Clean Architecture 架構設計：
 * <ul>
 *   <li>imrbs-core: Domain + Application 層（框架無關）</li>
 *   <li>imrbs-infrastructure: Infrastructure 層（JPA, Redis, RabbitMQ）</li>
 *   <li>imrbs-web: Presentation 層（REST Controllers, Security）</li>
 * </ul>
 * 
 * @author IMRBS Team
 * @version 1.0.0
 * @since 2025-11-20
 */
@SpringBootApplication(scanBasePackages = {
    "tw.huangcti.imrbs.web",
    "tw.huangcti.imrbs.infrastructure",
    "tw.huangcti.imrbs.application"
})
@EnableCaching
@EnableAsync
@EnableScheduling
public class ImrbsApplication {

    /**
     * 應用程式進入點
     * 
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(ImrbsApplication.class, args);
    }
}
