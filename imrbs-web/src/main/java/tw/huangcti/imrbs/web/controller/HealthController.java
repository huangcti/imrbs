package tw.huangcti.imrbs.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康檢查 Controller
 * 
 * <p>提供系統狀態檢查端點，用於驗證應用程式是否正常運作
 * 
 * @author IMRBS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 健康檢查端點
     * 
     * @return 系統狀態資訊
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "IMRBS");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
