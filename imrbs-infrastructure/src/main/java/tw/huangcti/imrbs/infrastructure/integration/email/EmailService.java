package tw.huangcti.imrbs.infrastructure.integration.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.format.DateTimeFormatter;

/**
 * Email 通知服務
 * 職責: 透過 SMTP 發送預約相關通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(JavaMailSender.class)
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@imrbs.example.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_TIME_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 發送預約確認通知
     */
    public void sendReservationConfirmation(Reservation reservation, String roomName, String userName, String recipientEmail) {
        try {
            String subject = "會議室預約確認 - " + reservation.getMeetingTitle();
            String body = buildConfirmationEmailBody(reservation, roomName, userName);

            sendEmail(recipientEmail, subject, body);
            log.info("預約確認通知已發送: reservationId={}, recipient={}", 
                    reservation.getId(), recipientEmail);
        } catch (Exception e) {
            log.error("預約確認通知發送失敗: reservationId={}, recipient={}, error={}", 
                    reservation.getId(), recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送預約取消通知
     */
    public void sendReservationCancellation(Reservation reservation, String roomName, String userName, String recipientEmail, String reason) {
        try {
            String subject = "會議室預約取消 - " + reservation.getMeetingTitle();
            String body = buildCancellationEmailBody(reservation, roomName, userName, reason);

            sendEmail(recipientEmail, subject, body);
            log.info("預約取消通知已發送: reservationId={}, recipient={}", 
                    reservation.getId(), recipientEmail);
        } catch (Exception e) {
            log.error("預約取消通知發送失敗: reservationId={}, recipient={}, error={}", 
                    reservation.getId(), recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送會議提醒 (30 分鐘前)
     */
    public void sendMeetingReminder(Reservation reservation, String roomName, String userName, String recipientEmail) {
        try {
            String subject = "會議提醒 - " + reservation.getMeetingTitle();
            String body = buildReminderEmailBody(reservation, roomName, userName);

            sendEmail(recipientEmail, subject, body);
            log.info("會議提醒已發送: reservationId={}, recipient={}", 
                    reservation.getId(), recipientEmail);
        } catch (Exception e) {
            log.error("會議提醒發送失敗: reservationId={}, recipient={}, error={}", 
                    reservation.getId(), recipientEmail, e.getMessage(), e);
            // 提醒失敗不拋出異常,避免影響主流程
        }
    }

    /**
     * 發送 Email (底層方法)
     */
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    /**
     * 構建預約確認 Email 內容
     */
    private String buildConfirmationEmailBody(Reservation reservation, String roomName, String userName) {
        return String.format("""
                您好,%s
                
                您的會議室預約已成功確認!
                
                === 預約詳情 ===
                會議主題: %s
                會議室: %s
                時間: %s ~ %s
                參與者: %s
                預約編號: %s
                
                === 注意事項 ===
                - 請準時到場,逾時 15 分鐘系統將自動取消預約
                - 會議結束後請協助恢復會議室整潔
                - 如需取消預約,請至系統操作
                
                此為系統自動發送,請勿直接回覆。
                """,
                userName,
                reservation.getMeetingTitle(),
                roomName,
                reservation.getStartTime().format(DATE_TIME_FORMAT),
                reservation.getEndTime().format(DATE_TIME_FORMAT),
                reservation.getParticipants() != null ? reservation.getParticipants() : "無",
                reservation.getId()
        );
    }

    /**
     * 構建預約取消 Email 內容
     */
    private String buildCancellationEmailBody(Reservation reservation, String roomName, String userName, String reason) {
        return String.format("""
                您好,%s
                
                您的會議室預約已取消。
                
                === 預約詳情 ===
                會議主題: %s
                會議室: %s
                時間: %s ~ %s
                預約編號: %s
                
                === 取消原因 ===
                %s
                
                如有疑問,請聯繫系統管理員。
                
                此為系統自動發送,請勿直接回覆。
                """,
                userName,
                reservation.getMeetingTitle(),
                roomName,
                reservation.getStartTime().format(DATE_TIME_FORMAT),
                reservation.getEndTime().format(DATE_TIME_FORMAT),
                reservation.getId(),
                reason != null ? reason : "使用者主動取消"
        );
    }

    /**
     * 構建會議提醒 Email 內容
     */
    private String buildReminderEmailBody(Reservation reservation, String roomName, String userName) {
        return String.format("""
                您好,%s
                
                您的會議即將在 30 分鐘後開始!
                
                === 會議資訊 ===
                會議主題: %s
                會議室: %s
                開始時間: %s
                結束時間: %s
                參與者: %s
                
                請提前 5 分鐘到場準備。
                
                此為系統自動發送,請勿直接回覆。
                """,
                userName,
                reservation.getMeetingTitle(),
                roomName,
                reservation.getStartTime().format(DATE_TIME_FORMAT),
                reservation.getEndTime().format(DATE_TIME_FORMAT),
                reservation.getParticipants() != null ? reservation.getParticipants() : "無"
        );
    }
}
