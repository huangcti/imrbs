package tw.huangcti.imrbs.service;

import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.exception.ConflictException;
import tw.huangcti.imrbs.util.TimeRangeValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 衝突檢查服務
 */
@Service
public class ConflictChecker {

    /**
     * 檢查新預約是否與現有預約衝突
     * @param existingReservations 現有預約列表
     * @param roomId 房間 ID
     * @param date 日期
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param excludeId 排除的預約 ID（用於更新時排除自己）
     * @throws ConflictException 如果發生衝突
     */
    public void checkConflict(
            List<Reservation> existingReservations,
            String roomId,
            LocalDate date,
            java.time.LocalTime startTime,
            java.time.LocalTime endTime,
            String excludeId) {
        
        List<Reservation> conflicts = existingReservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
            .filter(r -> !r.getId().equals(excludeId))
            .filter(r -> r.getRoomId().equals(roomId))
            .filter(r -> r.getDate().equals(date))
            .filter(r -> TimeRangeValidator.overlaps(
                startTime, endTime,
                r.getStartTime(), r.getEndTime()
            ))
            .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            throw new ConflictException(
                "預約時間與現有預約衝突。衝突預約數: " + conflicts.size()
            );
        }
    }
}
