package tw.huangcti.imrbs.infrastructure.messaging.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.event.ReservationEventPublisher;
import tw.huangcti.imrbs.infrastructure.messaging.event.ReservationCancelledEvent;
import tw.huangcti.imrbs.infrastructure.messaging.event.ReservationUpdatedEvent;

import java.time.LocalDateTime;

/**
 * ReservationEventPublisherImpl - 預約事件發布者實作
 * 
 * 實作 Domain Layer 定義的 ReservationEventPublisher 介面
 * 使用 RabbitMQ 發送事件
 * 
 * Clean Architecture: Infrastructure Layer (Adapter)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(RabbitTemplate.class)
public class ReservationEventPublisherImpl implements ReservationEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE = "reservation.exchange";
    private static final String ROUTING_KEY_UPDATED = "reservation.updated";
    private static final String ROUTING_KEY_CANCELLED = "reservation.cancelled";
    
    @Override
    public void publishReservationUpdated(Long reservationId, Long userId, LocalDateTime updatedAt) {
        try {
            ReservationUpdatedEvent event = new ReservationUpdatedEvent(reservationId, userId, updatedAt);
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_UPDATED, event);
            log.info("預約修改事件已發布: reservationId={}", reservationId);
        } catch (Exception e) {
            log.error("發布預約修改事件失敗: reservationId={}", reservationId, e);
            // 不拋出異常,避免影響主流程
        }
    }
    
    @Override
    public void publishReservationCancelled(Long reservationId, Long userId, String cancellationReason, LocalDateTime cancelledAt) {
        try {
            ReservationCancelledEvent event = new ReservationCancelledEvent(reservationId, userId, cancellationReason, cancelledAt);
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CANCELLED, event);
            log.info("預約取消事件已發布: reservationId={}", reservationId);
        } catch (Exception e) {
            log.error("發布預約取消事件失敗: reservationId={}", reservationId, e);
            // 不拋出異常,避免影響主流程
        }
    }
}
