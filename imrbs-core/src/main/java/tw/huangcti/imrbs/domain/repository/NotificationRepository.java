package tw.huangcti.imrbs.domain.repository;

import tw.huangcti.imrbs.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * NotificationRepository 介面
 * 
 * 描述: 通知資料存取介面 (Domain Layer)
 * 
 * 實作位置: Infrastructure Layer (imrbs-infrastructure)
 */
public interface NotificationRepository {
    
    /**
     * 根據 ID 查詢通知
     * 
     * @param id 通知 ID
     * @return Optional<Notification>
     */
    Optional<Notification> findById(Long id);
    
    /**
     * 根據預約 ID 查詢通知清單
     * 
     * @param reservationId 預約 ID
     * @return List<Notification>
     */
    List<Notification> findByReservationId(Long reservationId);
    
    /**
     * 根據收件人 Email 查詢通知清單
     * 
     * @param recipientEmail 收件人 Email
     * @return List<Notification>
     */
    List<Notification> findByRecipientEmail(String recipientEmail);
    
    /**
     * 根據狀態查詢通知清單
     * 
     * @param status 通知狀態 (PENDING, SENT, FAILED)
     * @return List<Notification>
     */
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    /**
     * 查詢所有待發送的通知 (status = PENDING)
     * 
     * @return List<Notification>
     */
    List<Notification> findPendingNotifications();
    
    /**
     * 查詢所有待重試的通知 (status = PENDING, retryCount < 3)
     * 
     * @return List<Notification>
     */
    List<Notification> findNotificationsForRetry();
    
    /**
     * 根據通知類型查詢通知清單
     * 
     * @param notificationType 通知類型
     * @return List<Notification>
     */
    List<Notification> findByNotificationType(Notification.NotificationType notificationType);
    
    /**
     * 查詢指定時間範圍內創建的通知
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<Notification>
     */
    List<Notification> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查詢發送失敗的通知 (status = FAILED)
     * 
     * @return List<Notification>
     */
    List<Notification> findFailedNotifications();
    
    /**
     * 儲存通知 (新增或更新)
     * 
     * @param notification 通知實體
     * @return 儲存後的通知實體
     */
    Notification save(Notification notification);
    
    /**
     * 刪除通知 (硬刪除)
     * 
     * @param id 通知 ID
     */
    void deleteById(Long id);
    
    /**
     * 查詢所有通知
     * 
     * @return List<Notification>
     */
    List<Notification> findAll();
    
    /**
     * 刪除指定時間之前的已發送通知 (清理舊資料)
     * 
     * @param beforeDate 指定日期
     * @return 刪除的記錄數
     */
    int deleteOldNotifications(LocalDateTime beforeDate);
}
