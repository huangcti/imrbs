package tw.huangcti.imrbs.infrastructure.messaging.event;

import java.time.LocalDateTime;

/**
 * ReservationUpdatedEvent - 預約修改事件
 * 
 * 當預約被修改時發送到 RabbitMQ
 * 
 * @param reservationId 預約 ID
 * @param userId 使用者 ID
 * @param updatedAt 修改時間
 */
public record ReservationUpdatedEvent(
        Long reservationId,
        Long userId,
        LocalDateTime updatedAt
) {}
