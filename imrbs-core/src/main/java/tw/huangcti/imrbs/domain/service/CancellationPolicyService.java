package tw.huangcti.imrbs.domain.service;

import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * CancellationPolicyService - 取消政策檢查服務
 * 
 * 負責驗證預約取消規則:
 * - 必須在會議開始前 24 小時取消
 * - 不能取消已取消或已完成的預約
 * - 不能取消已經開始的會議
 * 
 * Domain Service (框架無關)
 */
public class CancellationPolicyService {
    
    private static final int CANCELLATION_HOURS_BEFORE = 24;
    
    /**
     * 驗證是否可以取消預約
     * 
     * @param reservation 預約資訊
     * @param currentTime 當前時間
     * @throws ValidationException 如果不符合取消規則
     */
    public void validateCancellation(Reservation reservation, LocalDateTime currentTime) {
        // 檢查預約狀態
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new ValidationException("預約已取消，無法再次取消");
        }
        
        if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            throw new ValidationException("預約已完成，無法取消");
        }
        
        // 檢查是否在取消期限內
        if (!isWithinCancellationPeriod(reservation.getStartTime(), currentTime)) {
            LocalDateTime deadline = calculateCancellationDeadline(reservation.getStartTime());
            throw new ValidationException(
                String.format("無法取消：距離會議開始不足 24 小時（取消截止時間：%s）", deadline)
            );
        }
    }
    
    /**
     * 計算取消截止時間
     * 
     * @param meetingStartTime 會議開始時間
     * @return 取消截止時間（會議開始前 24 小時）
     */
    public LocalDateTime calculateCancellationDeadline(LocalDateTime meetingStartTime) {
        return meetingStartTime.minusHours(CANCELLATION_HOURS_BEFORE);
    }
    
    /**
     * 檢查是否在取消期限內
     * 
     * @param meetingStartTime 會議開始時間
     * @param currentTime 當前時間
     * @return true 如果可以取消（距離開始 >= 24 小時）
     */
    public boolean isWithinCancellationPeriod(LocalDateTime meetingStartTime, LocalDateTime currentTime) {
        Duration timeUntilMeeting = Duration.between(currentTime, meetingStartTime);
        long hoursUntilMeeting = timeUntilMeeting.toHours();
        return hoursUntilMeeting >= CANCELLATION_HOURS_BEFORE;
    }
}
