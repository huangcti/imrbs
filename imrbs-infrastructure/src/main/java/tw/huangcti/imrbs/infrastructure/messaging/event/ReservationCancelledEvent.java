package tw.huangcti.imrbs.infrastructure.messaging.event;

import java.time.LocalDateTime;

/**
 * ReservationCancelledEvent - 預約取消事件
 * 
 * 當預約被取消時發送到 RabbitMQ
 * 
 * @param reservationId 預約 ID
 * @param userId 使用者 ID
 * @param cancellationReason 取消原因
 * @param cancelledAt 取消時間
 */
public record ReservationCancelledEvent(
        Long reservationId,
        Long userId,
        String cancellationReason,
        LocalDateTime cancelledAt
) {}
