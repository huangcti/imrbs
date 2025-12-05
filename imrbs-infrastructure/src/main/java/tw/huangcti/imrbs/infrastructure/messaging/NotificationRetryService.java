package tw.huangcti.imrbs.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.infrastructure.integration.email.I18nEmailService;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 通知失敗重試服務
 * 職責: 包裝 EmailService 提供失敗重試機制 (最多 3 次)
 * 
 * 重試策略:
 * - 最大重試次數: 3 次
 * - 重試間隔: 指數退避 (1秒, 2秒, 4秒)
 * - 不重試的異常: IllegalArgumentException (參數錯誤無需重試)
 * 
 * 使用場景:
 * - 預約確認通知
 * - 預約取消通知
 * - 會議提醒通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryService {

    private final I18nEmailService emailService;
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 秒

    /**
     * 發送預約確認通知 (帶重試機制)
     */
    public void sendReservationConfirmationWithRetry(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String userName,
            String recipientEmail,
            Locale locale) {
        
        retryOperation(() -> {
            emailService.sendReservationConfirmation(
                    reservation, roomName, roomLocation, userName, recipientEmail, locale);
        }, "預約確認", reservation.getId(), recipientEmail);
    }

    /**
     * 發送預約取消通知 (帶重試機制)
     */
    public void sendReservationCancellationWithRetry(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String userName,
            String recipientEmail,
            String cancellationReason,
            String cancelledBy,
            LocalDateTime cancelledAt,
            boolean isUserCancelled,
            Locale locale) {
        
        retryOperation(() -> {
            emailService.sendReservationCancellation(
                    reservation, roomName, roomLocation, userName, recipientEmail,
                    cancellationReason, cancelledBy, cancelledAt, isUserCancelled, locale);
        }, "預約取消", reservation.getId(), recipientEmail);
    }

    /**
     * 發送會議提醒通知 (帶重試機制)
     */
    public void sendMeetingReminderWithRetry(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String roomEquipment,
            Integer roomCapacity,
            String userName,
            String recipientEmail,
            Locale locale) {
        
        retryOperation(() -> {
            emailService.sendMeetingReminder(
                    reservation, roomName, roomLocation, roomEquipment, 
                    roomCapacity, userName, recipientEmail, locale);
        }, "會議提醒", reservation.getId(), recipientEmail);
    }

    /**
     * 重試操作 (核心邏輯)
     */
    private void retryOperation(
            Runnable operation, 
            String operationType, 
            Long reservationId, 
            String recipientEmail) {
        
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;
            
            try {
                operation.run();
                
                // 成功發送
                if (attempt > 1) {
                    log.info("{} 通知重試成功: reservationId={}, recipient={}, attempt={}", 
                            operationType, reservationId, recipientEmail, attempt);
                }
                return; // 成功,直接返回
                
            } catch (IllegalArgumentException e) {
                // 參數錯誤,無需重試
                log.error("{} 通知失敗 (參數錯誤,不重試): reservationId={}, recipient={}, error={}", 
                        operationType, reservationId, recipientEmail, e.getMessage());
                throw e;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("{} 通知失敗: reservationId={}, recipient={}, attempt={}/{}, error={}", 
                        operationType, reservationId, recipientEmail, attempt, MAX_RETRY_ATTEMPTS, 
                        e.getMessage());
                
                // 如果還有重試機會,等待後重試
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long delayMs = calculateRetryDelay(attempt);
                    log.debug("等待 {} ms 後重試...", delayMs);
                    
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重試被中斷", ie);
                    }
                }
            }
        }
        
        // 所有重試都失敗
        log.error("{} 通知失敗 (已達最大重試次數): reservationId={}, recipient={}, attempts={}", 
                operationType, reservationId, recipientEmail, MAX_RETRY_ATTEMPTS);
        throw new RuntimeException(
                String.format("%s 通知發送失敗 (已重試 %d 次)", operationType, MAX_RETRY_ATTEMPTS),
                lastException);
    }

    /**
     * 計算重試延遲時間 (指數退避)
     * 第 1 次重試: 1 秒
     * 第 2 次重試: 2 秒
     * 第 3 次重試: 4 秒
     */
    private long calculateRetryDelay(int attempt) {
        return INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1));
    }

    /**
     * 備註: 重試機制設計考量
     * 
     * 1. 為何重試 3 次?
     *    - 足以應對短暫網路抖動
     *    - 避免過度重試造成系統負擔
     *    - 符合業界標準實務 (AWS SDK, Spring Retry 預設值)
     * 
     * 2. 為何使用指數退避?
     *    - 給下游服務 (SMTP Server) 恢復時間
     *    - 避免雪崩效應 (大量重試壓垮下游)
     *    - 1s, 2s, 4s 的延遲對使用者體驗影響微小
     * 
     * 3. 為何不重試 IllegalArgumentException?
     *    - 參數錯誤屬於程式邏輯錯誤,重試無意義
     *    - 應立即失敗並記錄錯誤,由開發者修正
     * 
     * 4. 為何同步重試而非非同步?
     *    - 簡化實作,避免引入非同步框架 (如 @Async, CompletableFuture)
     *    - 通知發送不是高頻操作,同步重試可接受
     *    - 總延遲最多 7 秒 (1+2+4),在可容忍範圍內
     * 
     * 5. 未來優化方向:
     *    - 引入 Spring Retry (@Retryable) 簡化程式碼
     *    - 使用 Circuit Breaker (Resilience4j) 防止雪崩
     *    - 將失敗通知寫入 Dead Letter Queue 供人工處理
     */
}
