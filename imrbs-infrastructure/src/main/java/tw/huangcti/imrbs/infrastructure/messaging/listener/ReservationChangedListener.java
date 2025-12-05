package tw.huangcti.imrbs.infrastructure.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.infrastructure.integration.email.ReservationChangeNotificationService;
import tw.huangcti.imrbs.infrastructure.messaging.event.ReservationUpdatedEvent;
import tw.huangcti.imrbs.infrastructure.messaging.event.ReservationCancelledEvent;

/**
 * ReservationChangedListener - 預約變更事件監聽器
 * 
 * 監聽 RabbitMQ 預約變更事件,並發送 Email 通知
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(ReservationChangeNotificationService.class)
public class ReservationChangedListener {
    
    private final ReservationChangeNotificationService notificationService;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    
    /**
     * 處理預約修改事件
     * 
     * @param event 預約修改事件
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.reservation-updated:reservation.updated}")
    public void handleReservationUpdated(ReservationUpdatedEvent event) {
        try {
            log.info("收到預約修改事件: reservationId={}", event.reservationId());
            
            Reservation reservation = reservationRepository.findById(event.reservationId())
                    .orElseThrow(() -> new IllegalStateException("找不到預約: " + event.reservationId()));
            
            User user = userRepository.findById(reservation.getUserId())
                    .orElseThrow(() -> new IllegalStateException("找不到使用者: " + reservation.getUserId()));
            
            notificationService.sendReservationUpdatedNotification(reservation, user);
            
        } catch (Exception e) {
            log.error("處理預約修改事件失敗: {}", event, e);
            // 不拋出異常,避免訊息重試
        }
    }
    
    /**
     * 處理預約取消事件
     * 
     * @param event 預約取消事件
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.reservation-cancelled:reservation.cancelled}")
    public void handleReservationCancelled(ReservationCancelledEvent event) {
        try {
            log.info("收到預約取消事件: reservationId={}", event.reservationId());
            
            Reservation reservation = reservationRepository.findById(event.reservationId())
                    .orElseThrow(() -> new IllegalStateException("找不到預約: " + event.reservationId()));
            
            User user = userRepository.findById(reservation.getUserId())
                    .orElseThrow(() -> new IllegalStateException("找不到使用者: " + reservation.getUserId()));
            
            notificationService.sendReservationCancelledNotification(reservation, user);
            
        } catch (Exception e) {
            log.error("處理預約取消事件失敗: {}", event, e);
            // 不拋出異常,避免訊息重試
        }
    }
}
