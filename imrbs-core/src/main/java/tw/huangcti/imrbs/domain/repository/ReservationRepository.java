package tw.huangcti.imrbs.domain.repository;

import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReservationRepository 介面
 * 
 * 描述: 預約資料存取介面 (Domain Layer)
 * 
 * 實作位置: Infrastructure Layer (imrbs-infrastructure)
 */
public interface ReservationRepository {
    
    /**
     * 根據 ID 查詢預約
     * 
     * @param id 預約 ID
     * @return Optional<Reservation>
     */
    Optional<Reservation> findById(Long id);
    
    /**
     * 根據使用者 ID 查詢預約清單
     * 
     * @param userId 使用者 ID
     * @return List<Reservation>
     */
    List<Reservation> findByUserId(Long userId);
    
    /**
     * 根據使用者 ID 和狀態查詢預約清單
     * 
     * @param userId 使用者 ID
     * @param status 預約狀態
     * @return List<Reservation>
     */
    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    
    /**
     * 根據會議室 ID 查詢預約清單
     * 
     * @param roomId 會議室 ID
     * @return List<Reservation>
     */
    List<Reservation> findByRoomId(Long roomId);
    
    /**
     * 根據會議室 ID 和時間範圍查詢預約清單
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<Reservation>
     */
    List<Reservation> findByRoomIdAndTimeRange(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    
    /**
     * 檢查會議室在指定時間區間是否有衝突的預約
     * 排除: 已取消的預約
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param excludeReservationId 排除的預約 ID (用於修改預約時排除自己)
     * @return true 如果有衝突
     */
    boolean hasConflict(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeReservationId
    );
    
    /**
     * 查詢指定時間區間內即將開始的預約 (用於發送提醒)
     * 條件: startTime 在 [now, now + reminderMinutes] 範圍內，且狀態為 CONFIRMED
     * 
     * @param reminderMinutes 提醒時間 (分鐘)，預設 30
     * @return List<Reservation>
     */
    List<Reservation> findUpcomingReservationsForReminder(int reminderMinutes);
    
    /**
     * 查詢使用者的未來預約 (startTime > now, status = CONFIRMED)
     * 
     * @param userId 使用者 ID
     * @return List<Reservation>
     */
    List<Reservation> findUpcomingReservationsByUserId(Long userId);
    
    /**
     * 查詢使用者的歷史預約 (endTime < now)
     * 
     * @param userId 使用者 ID
     * @return List<Reservation>
     */
    List<Reservation> findPastReservationsByUserId(Long userId);
    
    /**
     * 查詢待審核的預約 (status = PENDING)
     * 
     * @return List<Reservation>
     */
    List<Reservation> findPendingReservations();
    
    /**
     * 儲存預約 (新增或更新)
     * 
     * @param reservation 預約實體
     * @return 儲存後的預約實體
     */
    Reservation save(Reservation reservation);
    
    /**
     * 刪除預約 (硬刪除，謹慎使用)
     * 
     * @param id 預約 ID
     */
    void deleteById(Long id);
    
    /**
     * 查詢所有預約 (包含已取消)
     * 
     * @return List<Reservation>
     */
    List<Reservation> findAll();
    
    /**
     * 根據外部會議連結查詢預約
     * 
     * @param externalMeetingLink 外部會議連結
     * @return Optional<Reservation>
     */
    Optional<Reservation> findByExternalMeetingLink(String externalMeetingLink);
}
