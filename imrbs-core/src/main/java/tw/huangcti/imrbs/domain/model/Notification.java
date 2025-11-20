package tw.huangcti.imrbs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification 領域模型
 * 
 * 描述: 系統發送的通知記錄 (Email/SMS)
 * 
 * 業務規則:
 * - 通知由 RabbitMQ 消費者非同步處理
 * - status = PENDING 的通知會被排程任務定期掃描並發送
 * - 發送失敗時，retryCount++，最多重試 3 次
 * - 超過 3 次失敗後，status = FAILED，記錄 errorMessage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    /**
     * 主鍵 (自增)
     */
    private Long id;
    
    /**
     * 相關預約 ID (外鍵，可為空)
     */
    private Long reservationId;
    
    /**
     * 收件人 Email
     */
    private String recipientEmail;
    
    /**
     * 通知類型
     */
    private NotificationType notificationType;
    
    /**
     * 郵件主旨
     */
    private String subject;
    
    /**
     * 郵件內容
     */
    private String body;
    
    /**
     * 發送時間
     */
    private LocalDateTime sentAt;
    
    /**
     * 狀態: PENDING, SENT, FAILED
     */
    private NotificationStatus status;
    
    /**
     * 錯誤訊息 (失敗時記錄)
     */
    private String errorMessage;
    
    /**
     * 重試次數
     */
    @Builder.Default
    private Integer retryCount = 0;
    
    /**
     * 創建時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 通知類型枚舉
     */
    public enum NotificationType {
        /**
         * 預約成功通知
         */
        RESERVATION_CREATED,
        
        /**
         * 預約修改通知
         */
        RESERVATION_UPDATED,
        
        /**
         * 預約取消通知
         */
        RESERVATION_CANCELLED,
        
        /**
         * 會議前 30 分鐘提醒
         */
        MEETING_REMINDER,
        
        /**
         * 訪客預約審核通知 (發給管理員)
         */
        GUEST_APPROVAL_REQUEST,
        
        /**
         * 訪客預約審核結果 (發給訪客)
         */
        GUEST_APPROVAL_RESULT
    }
    
    /**
     * 通知狀態枚舉
     */
    public enum NotificationStatus {
        /**
         * 待發送
         */
        PENDING,
        
        /**
         * 已發送
         */
        SENT,
        
        /**
         * 發送失敗
         */
        FAILED
    }
    
    /**
     * 業務方法: 檢查通知是否待發送
     * 
     * @return true 如果狀態為 PENDING
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }
    
    /**
     * 業務方法: 檢查通知是否已發送
     * 
     * @return true 如果狀態為 SENT
     */
    public boolean isSent() {
        return status == NotificationStatus.SENT;
    }
    
    /**
     * 業務方法: 檢查通知是否發送失敗
     * 
     * @return true 如果狀態為 FAILED
     */
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }
    
    /**
     * 業務方法: 檢查是否還可以重試
     * 規則: 最多重試 3 次
     * 
     * @return true 如果 retryCount < 3
     */
    public boolean canRetry() {
        return retryCount < 3;
    }
    
    /**
     * 業務方法: 標記為發送成功
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    /**
     * 業務方法: 標記為發送失敗並增加重試次數
     * 
     * @param errorMsg 錯誤訊息
     */
    public void markAsFailed(String errorMsg) {
        this.errorMessage = errorMsg;
        this.retryCount++;
        
        if (canRetry()) {
            this.status = NotificationStatus.PENDING;
        } else {
            this.status = NotificationStatus.FAILED;
        }
    }
    
    /**
     * 業務方法: 檢查通知是否為會議提醒
     * 
     * @return true 如果類型為 MEETING_REMINDER
     */
    public boolean isMeetingReminder() {
        return notificationType == NotificationType.MEETING_REMINDER;
    }
    
    /**
     * 業務方法: 檢查通知是否與預約相關
     * 
     * @return true 如果 reservationId 不為空
     */
    public boolean isReservationRelated() {
        return reservationId != null;
    }
    
    /**
     * 業務方法: 檢查通知是否為訪客審核相關
     * 
     * @return true 如果類型為 GUEST_APPROVAL_REQUEST 或 GUEST_APPROVAL_RESULT
     */
    public boolean isGuestApprovalRelated() {
        return notificationType == NotificationType.GUEST_APPROVAL_REQUEST
                || notificationType == NotificationType.GUEST_APPROVAL_RESULT;
    }
}
