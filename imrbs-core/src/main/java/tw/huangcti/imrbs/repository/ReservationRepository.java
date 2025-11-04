package tw.huangcti.imrbs.repository;

import tw.huangcti.imrbs.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 預約儲存介面
 */
public interface ReservationRepository {
    
    /**
     * 儲存預約
     */
    Reservation save(Reservation reservation);
    
    /**
     * 根據 ID 查詢預約
     */
    Optional<Reservation> findById(String id);
    
    /**
     * 查詢指定房間、指定日期的所有預約
     */
    List<Reservation> findByRoomIdAndDate(String roomId, LocalDate date);
    
    /**
     * 查詢所有預約
     */
    List<Reservation> findAll();
    
    /**
     * 刪除預約
     */
    void deleteById(String id);
}
