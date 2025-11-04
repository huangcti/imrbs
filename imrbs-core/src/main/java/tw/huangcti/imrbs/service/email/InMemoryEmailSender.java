package tw.huangcti.imrbs.service.email;

import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.Reservation;

/**
 * 記憶體內的 Email 發送實作（測試替身）
 * 實際寄送可由其他實作（如 SmtpEmailService）取代
 */
@Service
public class InMemoryEmailSender implements EmailService {

    @Override
    public void sendReservationConfirmation(Reservation reservation) {
        System.out.println("[EMAIL] 發送預約確認至: " + reservation.getOrganizerEmail());
        System.out.println("  預約 ID: " + reservation.getId());
        System.out.println("  主題: " + reservation.getTitle());
    }

    @Override
    public void sendReservationUpdate(Reservation reservation) {
        System.out.println("[EMAIL] 發送預約更新至: " + reservation.getOrganizerEmail());
        System.out.println("  預約 ID: " + reservation.getId());
    }

    @Override
    public void sendReservationCancellation(Reservation reservation) {
        System.out.println("[EMAIL] 發送預約取消至: " + reservation.getOrganizerEmail());
        System.out.println("  預約 ID: " + reservation.getId());
    }
}
