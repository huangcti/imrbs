package tw.huangcti.imrbs.infrastructure.integration.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tw.huangcti.imrbs.domain.model.Reservation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * 多語系 Email 通知服務
 * 職責: 透過 Thymeleaf 模板引擎發送多語系 HTML 郵件
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(JavaMailSender.class)
public class I18nEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${app.email.from:noreply@imrbs.example.com}")
    private String fromEmail;

    @Value("${app.email.support:support@imrbs.example.com}")
    private String supportEmail;

    @Value("${app.system.url:http://localhost:8080}")
    private String systemUrl;

    /**
     * 發送預約確認通知 (支援多語系)
     */
    public void sendReservationConfirmation(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String userName,
            String recipientEmail,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("userName", userName);
            context.setVariable("description", reservation.getDescription());
            context.setVariable("attendeesCount", reservation.getParticipants());
            context.setVariable("viewReservationUrl", systemUrl + "/reservations/" + reservation.getId());
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.reservation.confirmed.subject",
                    new Object[]{reservation.getMeetingTitle()},
                    locale);

            String htmlContent = templateEngine.process("email/reservation-confirmed", context);

            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("預約確認通知已發送: reservationId={}, recipient={}, locale={}", 
                    reservation.getId(), recipientEmail, locale);
        } catch (Exception e) {
            log.error("預約確認通知發送失敗: reservationId={}, recipient={}, error={}", 
                    reservation.getId(), recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送預約取消通知 (支援多語系)
     */
    public void sendReservationCancellation(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String userName,
            String recipientEmail,
            String cancellationReason,
            String cancelledBy,
            LocalDateTime cancelledAt,
            boolean isUserCancelled,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("userName", userName);
            context.setVariable("cancellationReason", cancellationReason);
            context.setVariable("cancelledBy", cancelledBy);
            context.setVariable("cancelledAt", cancelledAt != null ? cancelledAt : LocalDateTime.now());
            context.setVariable("isUserCancelled", isUserCancelled);
            context.setVariable("makeNewReservationUrl", systemUrl + "/reservations/new");
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.reservation.cancelled.subject",
                    new Object[]{reservation.getMeetingTitle()},
                    locale);

            String htmlContent = templateEngine.process("email/reservation-cancelled", context);

            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("預約取消通知已發送: reservationId={}, recipient={}, locale={}", 
                    reservation.getId(), recipientEmail, locale);
        } catch (Exception e) {
            log.error("預約取消通知發送失敗: reservationId={}, recipient={}, error={}", 
                    reservation.getId(), recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送會議提醒 (30 分鐘前, 支援多語系)
     */
    public void sendMeetingReminder(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String roomEquipment,
            Integer roomCapacity,
            String userName,
            String recipientEmail,
            Locale locale) {
        try {
            // 計算距離會議開始的分鐘數
            long minutesUntilStart = ChronoUnit.MINUTES.between(LocalDateTime.now(), reservation.getStartTime());

            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("roomEquipment", roomEquipment);
            context.setVariable("roomCapacity", roomCapacity);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("userName", userName);
            context.setVariable("attendeesCount", reservation.getParticipants());
            context.setVariable("minutesUntilStart", minutesUntilStart);
            context.setVariable("viewReservationUrl", systemUrl + "/reservations/" + reservation.getId());
            context.setVariable("directionsUrl", systemUrl + "/rooms/" + reservation.getRoomId() + "/directions");
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.reminder.subject",
                    new Object[]{reservation.getMeetingTitle()},
                    locale);

            String htmlContent = templateEngine.process("email/meeting-reminder", context);

            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("會議提醒已發送: reservationId={}, recipient={}, locale={}, minutesUntilStart={}", 
                    reservation.getId(), recipientEmail, locale, minutesUntilStart);
        } catch (Exception e) {
            log.error("會議提醒發送失敗: reservationId={}, recipient={}, error={}", 
                    reservation.getId(), recipientEmail, e.getMessage(), e);
            // 提醒失敗不拋出異常,避免影響主流程
        }
    }

    /**
     * 發送 HTML Email (底層方法)
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML content

        mailSender.send(message);
    }

    /**
     * 解析語言偏好設定
     * 支援: zh-TW (繁體中文), en (English)
     */
    public Locale parseLocale(String languagePreference) {
        if (languagePreference == null || languagePreference.isBlank()) {
            return Locale.TRADITIONAL_CHINESE; // 預設繁體中文
        }

        return switch (languagePreference.toLowerCase()) {
            case "en", "en-us", "en-gb" -> Locale.ENGLISH;
            case "zh-tw", "zh-hant", "zh_tw" -> Locale.TRADITIONAL_CHINESE;
            case "zh-cn", "zh-hans", "zh_cn" -> Locale.SIMPLIFIED_CHINESE;
            default -> Locale.TRADITIONAL_CHINESE;
        };
    }
}
