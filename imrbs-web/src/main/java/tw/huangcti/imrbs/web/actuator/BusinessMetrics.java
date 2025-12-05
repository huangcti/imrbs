package tw.huangcti.imrbs.web.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 自訂業務指標收集器.
 * 
 * <p>用於收集預約相關的業務指標，提供給 Prometheus 監控。</p>
 */
@Component
public class BusinessMetrics {

    private final Counter reservationCreatedCounter;
    private final Counter reservationCancelledCounter;
    private final Counter reservationConflictCounter;
    private final Counter guestRequestCounter;
    private final Counter guestRequestApprovedCounter;
    private final Counter guestRequestRejectedCounter;
    private final Counter authLoginSuccessCounter;
    private final Counter authLoginFailureCounter;
    private final Timer reservationCreationTimer;
    private final Timer roomSearchTimer;

    public BusinessMetrics(MeterRegistry registry) {
        // 預約計數器
        this.reservationCreatedCounter = Counter.builder("imrbs.reservations.created")
                .description("Number of reservations created")
                .register(registry);

        this.reservationCancelledCounter = Counter.builder("imrbs.reservations.cancelled")
                .description("Number of reservations cancelled")
                .register(registry);

        this.reservationConflictCounter = Counter.builder("imrbs.reservations.conflicts")
                .description("Number of reservation conflicts detected")
                .register(registry);

        // 訪客申請計數器
        this.guestRequestCounter = Counter.builder("imrbs.guest.requests.total")
                .description("Total number of guest requests")
                .register(registry);

        this.guestRequestApprovedCounter = Counter.builder("imrbs.guest.requests.approved")
                .description("Number of approved guest requests")
                .register(registry);

        this.guestRequestRejectedCounter = Counter.builder("imrbs.guest.requests.rejected")
                .description("Number of rejected guest requests")
                .register(registry);

        // 認證計數器
        this.authLoginSuccessCounter = Counter.builder("imrbs.auth.login.success")
                .description("Number of successful logins")
                .register(registry);

        this.authLoginFailureCounter = Counter.builder("imrbs.auth.login.failure")
                .description("Number of failed logins")
                .register(registry);

        // 計時器
        this.reservationCreationTimer = Timer.builder("imrbs.reservations.creation.duration")
                .description("Time taken to create a reservation")
                .register(registry);

        this.roomSearchTimer = Timer.builder("imrbs.rooms.search.duration")
                .description("Time taken to search rooms")
                .register(registry);
    }

    // ==================== 預約相關指標 ====================

    /**
     * 記錄預約創建.
     */
    public void recordReservationCreated() {
        reservationCreatedCounter.increment();
    }

    /**
     * 記錄預約取消.
     */
    public void recordReservationCancelled() {
        reservationCancelledCounter.increment();
    }

    /**
     * 記錄預約衝突.
     */
    public void recordReservationConflict() {
        reservationConflictCounter.increment();
    }

    /**
     * 記錄預約創建耗時.
     *
     * @param durationMs 耗時 (毫秒)
     */
    public void recordReservationCreationTime(long durationMs) {
        reservationCreationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    // ==================== 訪客申請相關指標 ====================

    /**
     * 記錄訪客申請提交.
     */
    public void recordGuestRequest() {
        guestRequestCounter.increment();
    }

    /**
     * 記錄訪客申請批准.
     */
    public void recordGuestRequestApproved() {
        guestRequestApprovedCounter.increment();
    }

    /**
     * 記錄訪客申請拒絕.
     */
    public void recordGuestRequestRejected() {
        guestRequestRejectedCounter.increment();
    }

    // ==================== 認證相關指標 ====================

    /**
     * 記錄登入成功.
     */
    public void recordLoginSuccess() {
        authLoginSuccessCounter.increment();
    }

    /**
     * 記錄登入失敗.
     */
    public void recordLoginFailure() {
        authLoginFailureCounter.increment();
    }

    // ==================== 搜尋相關指標 ====================

    /**
     * 記錄會議室搜尋耗時.
     *
     * @param durationMs 耗時 (毫秒)
     */
    public void recordRoomSearchTime(long durationMs) {
        roomSearchTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
}
