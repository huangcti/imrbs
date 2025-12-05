package tw.huangcti.imrbs.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 安全標頭過濾器.
 * 
 * <p>為所有回應添加安全相關的 HTTP 標頭，防止常見的 Web 攻擊：</p>
 * <ul>
 *   <li>XSS (跨站腳本攻擊)</li>
 *   <li>Clickjacking (點擊劫持)</li>
 *   <li>MIME 類型混淆攻擊</li>
 *   <li>資訊洩漏</li>
 * </ul>
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // ==================== 防止 XSS 攻擊 ====================
        
        // Content-Security-Policy: 限制資源載入來源
        response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self' http://localhost:* ws://localhost:*; " +
                "frame-ancestors 'self';"
        );
        
        // X-XSS-Protection: 啟用瀏覽器 XSS 過濾器 (舊版瀏覽器)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // ==================== 防止 Clickjacking ====================
        
        // X-Frame-Options: 防止頁面被嵌入 iframe
        response.setHeader("X-Frame-Options", "DENY");
        
        // ==================== 防止 MIME 類型混淆 ====================
        
        // X-Content-Type-Options: 禁止瀏覽器猜測 MIME 類型
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // ==================== 控制 Referrer 資訊 ====================
        
        // Referrer-Policy: 控制 Referrer 標頭的內容
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // ==================== 強制 HTTPS (生產環境) ====================
        
        // Strict-Transport-Security: 強制使用 HTTPS
        // 注意: 開發環境不應啟用，否則 localhost 也會被要求使用 HTTPS
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        
        // ==================== 權限策略 ====================
        
        // Permissions-Policy: 限制瀏覽器功能
        response.setHeader("Permissions-Policy", 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "accelerometer=(), " +
                "gyroscope=()"
        );
        
        // ==================== 快取控制 (敏感端點) ====================
        
        String path = request.getRequestURI();
        if (isSensitivePath(path)) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
        // ==================== 隱藏伺服器資訊 ====================
        
        // 移除或覆蓋可能洩漏伺服器資訊的標頭
        response.setHeader("Server", "IMRBS");
        response.setHeader("X-Powered-By", "");
        
        filterChain.doFilter(request, response);
    }

    /**
     * 判斷是否為敏感路徑.
     */
    private boolean isSensitivePath(String path) {
        return path.startsWith("/api/v1/auth") 
                || path.startsWith("/api/v1/users")
                || path.startsWith("/api/v1/admin");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Swagger UI 需要特殊處理
        return path.startsWith("/swagger-ui") || path.startsWith("/api-docs");
    }
}
