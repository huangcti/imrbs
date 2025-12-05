package tw.huangcti.imrbs.domain.event;

import java.time.LocalDateTime;

/**
 * ReservationEventPublisher - 預約事件發布介面 (Port)
 * 
 * Clean Architecture: Domain Layer 定義介面
 * Infrastructure Layer 實作具體發布邏輯
 */
public interface ReservationEventPublisher {
    
    /**
     * 發布預約修改事件
     * 
     * @param reservationId 預約 ID
     * @param userId 使用者 ID
     * @param updatedAt 修改時間
     */
    void publishReservationUpdated(Long reservationId, Long userId, LocalDateTime updatedAt);
    
    /**
     * 發布預約取消事件
     * 
     * @param reservationId 預約 ID
     * @param userId 使用者 ID
     * @param cancellationReason 取消原因
     * @param cancelledAt 取消時間
     */
    void publishReservationCancelled(Long reservationId, Long userId, String cancellationReason, LocalDateTime cancelledAt);
}
