package tw.huangcti.imrbs.web.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 健康檢查指標.
 * 
 * <p>檢查 Redis 連線狀態，用於 Actuator /health 端點。</p>
 */
@Component("redisHealthIndicator")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            var connection = connectionFactory.getConnection();
            String pong = connection.ping();
            connection.close();
            
            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connected")
                        .withDetail("ping", pong)
                        .build();
            }
            
            return Health.down()
                    .withDetail("cache", "Redis")
                    .withDetail("status", "Unexpected response")
                    .withDetail("ping", pong)
                    .build();
                    
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("cache", "Redis")
                    .withDetail("status", "Disconnected")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
