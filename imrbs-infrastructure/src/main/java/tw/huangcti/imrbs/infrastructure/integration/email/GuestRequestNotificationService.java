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
import java.util.Locale;

/**
 * 訪客審核通知服務
 * 職責: 發送訪客預約審核相關通知
 * 
 * 通知類型:
 * 1. 訪客預約申請提交 → 通知審核者 (Approver)
 * 2. 訪客預約審核通過 → 通知申請者 (Requester) 和訪客 (Guest)
 * 3. 訪客預約審核拒絕 → 通知申請者和訪客
 * 4. 訪客預約取消 → 通知所有相關人員
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(JavaMailSender.class)
public class GuestRequestNotificationService {

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
     * 發送訪客預約申請通知 (給審核者)
     */
    public void sendGuestRequestSubmittedToApprover(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String requesterName,
            String guestName,
            String guestEmail,
            String guestCompany,
            String approverName,
            String approverEmail,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("requesterName", requesterName);
            context.setVariable("guestName", guestName);
            context.setVariable("guestEmail", guestEmail);
            context.setVariable("guestCompany", guestCompany);
            context.setVariable("approverName", approverName);
            context.setVariable("description", reservation.getDescription());
            context.setVariable("approveUrl", systemUrl + "/guest-requests/" + reservation.getId() + "/approve");
            context.setVariable("rejectUrl", systemUrl + "/guest-requests/" + reservation.getId() + "/reject");
            context.setVariable("viewRequestUrl", systemUrl + "/guest-requests/" + reservation.getId());
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.guest.request.submitted.subject",
                    new Object[]{guestName, requesterName},
                    "訪客預約申請 - " + guestName + " (由 " + requesterName + " 申請)",
                    locale);

            String htmlContent = templateEngine.process("email/guest-request-submitted", context);

            sendHtmlEmail(approverEmail, subject, htmlContent);
            log.info("訪客預約申請通知已發送給審核者: reservationId={}, approver={}", 
                    reservation.getId(), approverEmail);
        } catch (Exception e) {
            log.error("訪客預約申請通知發送失敗: reservationId={}, approver={}, error={}", 
                    reservation.getId(), approverEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送訪客預約審核通過通知 (給申請者)
     */
    public void sendGuestRequestApprovedToRequester(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String requesterName,
            String requesterEmail,
            String guestName,
            String approverName,
            LocalDateTime approvedAt,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("requesterName", requesterName);
            context.setVariable("guestName", guestName);
            context.setVariable("approverName", approverName);
            context.setVariable("approvedAt", approvedAt);
            context.setVariable("viewReservationUrl", systemUrl + "/reservations/" + reservation.getId());
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.guest.request.approved.subject",
                    new Object[]{guestName},
                    "訪客預約已核准 - " + guestName,
                    locale);

            String htmlContent = templateEngine.process("email/guest-request-approved", context);

            sendHtmlEmail(requesterEmail, subject, htmlContent);
            log.info("訪客預約核准通知已發送給申請者: reservationId={}, requester={}", 
                    reservation.getId(), requesterEmail);
        } catch (Exception e) {
            log.error("訪客預約核准通知發送失敗: reservationId={}, requester={}, error={}", 
                    reservation.getId(), requesterEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送訪客預約審核通過通知 (給訪客)
     */
    public void sendGuestRequestApprovedToGuest(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String requesterName,
            String guestName,
            String guestEmail,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("requesterName", requesterName);
            context.setVariable("guestName", guestName);
            context.setVariable("description", reservation.getDescription());
            context.setVariable("directionsUrl", systemUrl + "/rooms/" + reservation.getRoomId() + "/directions");
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.guest.approved.subject",
                    new Object[]{roomName},
                    "您的訪客預約已核准 - " + roomName,
                    locale);

            String htmlContent = templateEngine.process("email/guest-approved", context);

            sendHtmlEmail(guestEmail, subject, htmlContent);
            log.info("訪客預約核准通知已發送給訪客: reservationId={}, guest={}", 
                    reservation.getId(), guestEmail);
        } catch (Exception e) {
            log.error("訪客預約核准通知發送失敗: reservationId={}, guest={}, error={}", 
                    reservation.getId(), guestEmail, e.getMessage(), e);
            // 訪客通知失敗不拋出異常,避免影響主流程
        }
    }

    /**
     * 發送訪客預約審核拒絕通知 (給申請者)
     */
    public void sendGuestRequestRejectedToRequester(
            Reservation reservation,
            String roomName,
            String roomLocation,
            String requesterName,
            String requesterEmail,
            String guestName,
            String approverName,
            String rejectionReason,
            LocalDateTime rejectedAt,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("roomLocation", roomLocation);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("requesterName", requesterName);
            context.setVariable("guestName", guestName);
            context.setVariable("approverName", approverName);
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("rejectedAt", rejectedAt);
            context.setVariable("makeNewRequestUrl", systemUrl + "/guest-requests/new");
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.guest.request.rejected.subject",
                    new Object[]{guestName},
                    "訪客預約已拒絕 - " + guestName,
                    locale);

            String htmlContent = templateEngine.process("email/guest-request-rejected", context);

            sendHtmlEmail(requesterEmail, subject, htmlContent);
            log.info("訪客預約拒絕通知已發送給申請者: reservationId={}, requester={}", 
                    reservation.getId(), requesterEmail);
        } catch (Exception e) {
            log.error("訪客預約拒絕通知發送失敗: reservationId={}, requester={}, error={}", 
                    reservation.getId(), requesterEmail, e.getMessage(), e);
            throw new RuntimeException("Email 發送失敗", e);
        }
    }

    /**
     * 發送訪客預約審核拒絕通知 (給訪客)
     */
    public void sendGuestRequestRejectedToGuest(
            Reservation reservation,
            String roomName,
            String guestName,
            String guestEmail,
            String rejectionReason,
            Locale locale) {
        try {
            Context context = new Context(locale);
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("meetingTitle", reservation.getMeetingTitle());
            context.setVariable("roomName", roomName);
            context.setVariable("startTime", reservation.getStartTime());
            context.setVariable("endTime", reservation.getEndTime());
            context.setVariable("guestName", guestName);
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("systemUrl", systemUrl);
            context.setVariable("supportEmail", supportEmail);

            String subject = messageSource.getMessage(
                    "email.guest.rejected.subject",
                    new Object[]{roomName},
                    "訪客預約已拒絕 - " + roomName,
                    locale);

            String htmlContent = templateEngine.process("email/guest-rejected", context);

            sendHtmlEmail(guestEmail, subject, htmlContent);
            log.info("訪客預約拒絕通知已發送給訪客: reservationId={}, guest={}", 
                    reservation.getId(), guestEmail);
        } catch (Exception e) {
            log.error("訪客預約拒絕通知發送失敗: reservationId={}, guest={}, error={}", 
                    reservation.getId(), guestEmail, e.getMessage(), e);
            // 訪客通知失敗不拋出異常,避免影響主流程
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
     * 備註: 訪客審核通知流程
     * 
     * 1. 申請階段 (Requester 提交訪客預約):
     *    → sendGuestRequestSubmittedToApprover() 通知審核者
     * 
     * 2. 審核通過 (Approver 核准):
     *    → sendGuestRequestApprovedToRequester() 通知申請者
     *    → sendGuestRequestApprovedToGuest() 通知訪客
     * 
     * 3. 審核拒絕 (Approver 拒絕):
     *    → sendGuestRequestRejectedToRequester() 通知申請者
     *    → sendGuestRequestRejectedToGuest() 通知訪客
     * 
     * 4. 預約取消 (已核准後取消):
     *    → 使用 I18nEmailService.sendReservationCancellation()
     *       (訪客預約與一般預約的取消流程相同)
     * 
     * 設計考量:
     * - 審核者通知: 必須成功 (拋出異常,由呼叫者處理)
     * - 申請者通知: 必須成功 (拋出異常)
     * - 訪客通知: 失敗時僅記錄 (不拋出異常,避免影響主流程)
     */
}
