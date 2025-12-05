package tw.huangcti.imrbs.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速率限制過濾器.
 * 
 * <p>實作滑動視窗速率限制演算法，防止 API 濫用。</p>
 * 
 * <p>預設限制：</p>
 * <ul>
 *   <li>一般 API: 每分鐘 100 次請求</li>
 *   <li>認證 API: 每分鐘 10 次請求</li>
 *   <li>訪客申請: 每分鐘 5 次請求</li>
 * </ul>
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // 預設速率限制 (每分鐘請求數)
    private static final int DEFAULT_RATE_LIMIT = 100;
    private static final int AUTH_RATE_LIMIT = 10;
    private static final int GUEST_RATE_LIMIT = 5;

    // 時間視窗 (毫秒)
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute

    // 請求計數器 (IP -> 計數資訊)
    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    // 清理過期計數器的時間間隔
    private long lastCleanupTime = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 300_000; // 5 minutes

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        
        // 確定速率限制
        int rateLimit = determineRateLimit(requestPath);
        
        // 定期清理過期計數器
        cleanupExpiredEntries();
        
        // 檢查速率限制
        RateLimitInfo info = requestCounts.computeIfAbsent(
                clientIp + ":" + getRateLimitCategory(requestPath),
                k -> new RateLimitInfo()
        );
        
        if (info.isRateLimited(rateLimit, WINDOW_SIZE_MS)) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, requestPath);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(info.getResetTime()));
            response.setHeader("Retry-After", String.valueOf(info.getRetryAfterSeconds()));
            
            response.getWriter().write(String.format(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again in %d seconds.\",\"retryAfter\":%d}",
                    info.getRetryAfterSeconds(),
                    info.getRetryAfterSeconds()
            ));
            return;
        }
        
        // 增加請求計數
        info.incrementCount();
        
        // 設定速率限制標頭
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, rateLimit - info.getCount())));
        response.setHeader("X-RateLimit-Reset", String.valueOf(info.getResetTime()));
        
        filterChain.doFilter(request, response);
    }

    /**
     * 取得客戶端 IP (考慮代理).
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 根據路徑確定速率限制.
     */
    private int determineRateLimit(String path) {
        if (path.startsWith("/api/v1/auth")) {
            return AUTH_RATE_LIMIT;
        }
        if (path.startsWith("/api/v1/guest")) {
            return GUEST_RATE_LIMIT;
        }
        return DEFAULT_RATE_LIMIT;
    }

    /**
     * 取得速率限制類別.
     */
    private String getRateLimitCategory(String path) {
        if (path.startsWith("/api/v1/auth")) {
            return "auth";
        }
        if (path.startsWith("/api/v1/guest")) {
            return "guest";
        }
        return "default";
    }

    /**
     * 清理過期的計數器.
     */
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            requestCounts.entrySet().removeIf(entry -> 
                    entry.getValue().isExpired(WINDOW_SIZE_MS * 2)
            );
            lastCleanupTime = now;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 排除健康檢查和 Actuator 端點
        return path.startsWith("/actuator") 
                || path.equals("/api/v1/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs");
    }

    /**
     * 速率限制資訊.
     */
    private static class RateLimitInfo {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        /**
         * 檢查是否超過速率限制.
         */
        public boolean isRateLimited(int limit, long windowSizeMs) {
            long now = System.currentTimeMillis();
            
            // 如果視窗已過期，重置計數
            if (now - windowStart > windowSizeMs) {
                synchronized (this) {
                    if (now - windowStart > windowSizeMs) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            
            return count.get() >= limit;
        }

        /**
         * 增加請求計數.
         */
        public void incrementCount() {
            count.incrementAndGet();
        }

        /**
         * 取得當前計數.
         */
        public int getCount() {
            return count.get();
        }

        /**
         * 取得重置時間 (Unix 時間戳，秒).
         */
        public long getResetTime() {
            return (windowStart + 60_000) / 1000;
        }

        /**
         * 取得重試等待秒數.
         */
        public int getRetryAfterSeconds() {
            long remaining = (windowStart + 60_000) - System.currentTimeMillis();
            return Math.max(1, (int) (remaining / 1000));
        }

        /**
         * 檢查是否已過期.
         */
        public boolean isExpired(long maxAgeMs) {
            return System.currentTimeMillis() - windowStart > maxAgeMs;
        }
    }
}
