package tw.huangcti.imrbs.infrastructure.integration.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.User;

import java.time.format.DateTimeFormatter;

/**
 * ReservationChangeNotificationService - 預約變更通知服務
 * 
 * 負責發送預約修改和取消的 Email 通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(JavaMailSender.class)
public class ReservationChangeNotificationService {
    
    private final JavaMailSender mailSender;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * 發送預約修改通知
     * 
     * @param reservation 修改後的預約
     * @param user 預約者
     */
    public void sendReservationUpdatedNotification(Reservation reservation, User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("【會議室系統】預約已修改");
            message.setText(buildUpdatedEmailContent(reservation, user));
            
            mailSender.send(message);
            log.info("預約修改通知已發送: reservationId={}, userId={}", 
                    reservation.getId(), user.getId());
        } catch (Exception e) {
            log.error("發送預約修改通知失敗: reservationId={}", reservation.getId(), e);
            // 不拋出異常，避免影響主流程
        }
    }
    
    /**
     * 發送預約取消通知
     * 
     * @param reservation 已取消的預約
     * @param user 預約者
     */
    public void sendReservationCancelledNotification(Reservation reservation, User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("【會議室系統】預約已取消");
            message.setText(buildCancelledEmailContent(reservation, user));
            
            mailSender.send(message);
            log.info("預約取消通知已發送: reservationId={}, userId={}", 
                    reservation.getId(), user.getId());
        } catch (Exception e) {
            log.error("發送預約取消通知失敗: reservationId={}", reservation.getId(), e);
            // 不拋出異常，避免影響主流程
        }
    }
    
    private String buildUpdatedEmailContent(Reservation reservation, User user) {
        return String.format("""
                您好 %s,
                
                您的會議室預約已成功修改：
                
                會議主題：%s
                會議時間：%s ~ %s
                預約編號：%s
                
                如需再次修改或取消預約，請登入會議室預約系統。
                
                此為系統自動發送郵件，請勿回覆。
                """,
                user.getFullName(),
                reservation.getMeetingTitle(),
                reservation.getStartTime().format(FORMATTER),
                reservation.getEndTime().format(FORMATTER),
                reservation.getId()
        );
    }
    
    private String buildCancelledEmailContent(Reservation reservation, User user) {
        return String.format("""
                您好 %s,
                
                您的會議室預約已成功取消：
                
                會議主題：%s
                原訂時間：%s ~ %s
                取消原因：%s
                預約編號：%s
                
                如需重新預約，請登入會議室預約系統。
                
                此為系統自動發送郵件，請勿回覆。
                """,
                user.getFullName(),
                reservation.getMeetingTitle(),
                reservation.getStartTime().format(FORMATTER),
                reservation.getEndTime().format(FORMATTER),
                reservation.getCancellationReason() != null ? reservation.getCancellationReason() : "未提供",
                reservation.getId()
        );
    }
}
