package tw.huangcti.imrbs.web.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 資料庫健康檢查指標.
 * 
 * <p>檢查資料庫連線狀態，用於 Actuator /health 端點。</p>
 */
@Component("databaseHealthIndicator")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute("SELECT 1");
            
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
                    
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Disconnected")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
