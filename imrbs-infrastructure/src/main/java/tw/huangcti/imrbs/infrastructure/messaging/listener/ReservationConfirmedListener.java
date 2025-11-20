package tw.huangcti.imrbs.infrastructure.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Notification;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.NotificationRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.infrastructure.integration.email.EmailService;

import java.time.LocalDateTime;

/**
 * 預約確認事件監聽器
 * 職責: 監聽 RabbitMQ 預約確認事件,觸發 Email 通知
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationConfirmedListener {

    private final EmailService emailService;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    /**
     * 監聽預約確認通知 (Email Queue)
     * Queue: notification.email.queue
     * Routing Key: email
     */
    @RabbitListener(queues = "notification.email.queue")
    public void handleReservationConfirmation(String message) {
        try {
            log.info("收到預約確認通知: {}", message);

            // 解析訊息 (假設格式為 JSON: {"reservationId": 123, "type": "CONFIRMATION"})
            NotificationMessage notificationMsg = objectMapper.readValue(message, NotificationMessage.class);

            // 查詢預約
            Reservation reservation = reservationRepository.findById(notificationMsg.reservationId())
                    .orElseThrow(() -> new RuntimeException("預約不存在: " + notificationMsg.reservationId()));

            // 發送 Email
            String recipientEmail = reservation.getUser().getEmail();
            
            if ("CONFIRMATION".equals(notificationMsg.type())) {
                emailService.sendReservationConfirmation(reservation, recipientEmail);
            } else if ("CANCELLATION".equals(notificationMsg.type())) {
                emailService.sendReservationCancellation(reservation, recipientEmail, notificationMsg.reason());
            }

            // 更新通知狀態
            updateNotificationStatus(notificationMsg.reservationId(), Notification.NotificationStatus.SENT);

            log.info("預約通知處理完成: reservationId={}", notificationMsg.reservationId());
        } catch (Exception e) {
            log.error("預約通知處理失敗: message={}, error={}", message, e.getMessage(), e);
            
            // 更新通知狀態為失敗 (會進入 Dead Letter Queue)
            try {
                NotificationMessage notificationMsg = objectMapper.readValue(message, NotificationMessage.class);
                updateNotificationStatus(notificationMsg.reservationId(), Notification.NotificationStatus.FAILED);
            } catch (Exception ex) {
                log.error("更新通知狀態失敗", ex);
            }
            
            throw new RuntimeException("通知處理失敗", e);
        }
    }

    /**
     * 監聽會議提醒 (Reminder Queue with 30-minute TTL)
     * Queue: notification.reminder.queue
     * Routing Key: reminder
     */
    @RabbitListener(queues = "notification.reminder.queue")
    public void handleMeetingReminder(String message) {
        try {
            log.info("收到會議提醒: {}", message);

            NotificationMessage notificationMsg = objectMapper.readValue(message, NotificationMessage.class);

            Reservation reservation = reservationRepository.findById(notificationMsg.reservationId())
                    .orElseThrow(() -> new RuntimeException("預約不存在: " + notificationMsg.reservationId()));

            // 檢查會議是否仍有效 (未取消)
            if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
                log.info("會議已取消,跳過提醒: reservationId={}", notificationMsg.reservationId());
                return;
            }

            // 發送提醒 Email
            String recipientEmail = reservation.getUser().getEmail();
            emailService.sendMeetingReminder(reservation, recipientEmail);

            // 如果有參與者,也發送給他們
            if (reservation.getParticipants() != null && !reservation.getParticipants().isEmpty()) {
                String[] participantEmails = reservation.getParticipants().split(",");
                for (String email : participantEmails) {
                    emailService.sendMeetingReminder(reservation, email.trim());
                }
            }

            log.info("會議提醒處理完成: reservationId={}", notificationMsg.reservationId());
        } catch (Exception e) {
            log.error("會議提醒處理失敗: message={}, error={}", message, e.getMessage(), e);
            // 提醒失敗不拋出異常,避免重試
        }
    }

    /**
     * 更新通知狀態
     */
    private void updateNotificationStatus(Long reservationId, Notification.NotificationStatus status) {
        try {
            notificationRepository.findByReservationId(reservationId)
                    .ifPresent(notification -> {
                        if (status == Notification.NotificationStatus.SENT) {
                            notification.markAsSent();
                        } else if (status == Notification.NotificationStatus.FAILED) {
                            notification.markAsFailed("Email 發送失敗");
                        }
                        notificationRepository.save(notification);
                    });
        } catch (Exception e) {
            log.error("更新通知狀態失敗: reservationId={}, status={}", reservationId, status, e);
        }
    }

    /**
     * 通知訊息 DTO
     */
    public record NotificationMessage(
            Long reservationId,
            String type, // CONFIRMATION, CANCELLATION, REMINDER
            String reason // 取消原因 (僅 CANCELLATION 類型使用)
    ) {}
}
