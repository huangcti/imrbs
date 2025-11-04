package tw.huangcti.imrbs.service.email;

import tw.huangcti.imrbs.domain.Reservation;

/**
 * Email 發送介面
 */
public interface EmailService {
    
    /**
     * 發送預約確認 email
     */
    void sendReservationConfirmation(Reservation reservation);
    
    /**
     * 發送預約更新 email
     */
    void sendReservationUpdate(Reservation reservation);
    
    /**
     * 發送預約取消 email
     */
    void sendReservationCancellation(Reservation reservation);
}
