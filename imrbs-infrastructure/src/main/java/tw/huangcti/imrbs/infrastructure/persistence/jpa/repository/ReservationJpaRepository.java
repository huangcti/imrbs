package tw.huangcti.imrbs.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.ReservationJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReservationJpaRepository - Spring Data JPA Repository
 * 
 * 描述: Reservation 的 Spring Data JPA 實作
 * 
 * 設計: Infrastructure Layer - 實作 Domain Layer 的 ReservationRepository 介面
 */
@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {
    
    /**
     * 根據使用者 ID 查詢預約清單
     * 
     * @param userId 使用者 ID
     * @return List<ReservationJpaEntity>
     */
    List<ReservationJpaEntity> findByUserId(Long userId);
    
    /**
     * 根據使用者 ID 和狀態查詢預約清單
     * 
     * @param userId 使用者 ID
     * @param status 預約狀態
     * @return List<ReservationJpaEntity>
     */
    List<ReservationJpaEntity> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    
    /**
     * 根據會議室 ID 查詢預約清單
     * 
     * @param roomId 會議室 ID
     * @return List<ReservationJpaEntity>
     */
    List<ReservationJpaEntity> findByRoomId(Long roomId);
    
    /**
     * 根據會議室 ID 和時間範圍查詢預約清單
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<ReservationJpaEntity>
     */
    @Query("""
        SELECT r FROM ReservationJpaEntity r
        WHERE r.roomId = :roomId
        AND r.startTime < :endTime
        AND r.endTime > :startTime
        AND r.status != 'CANCELLED'
        """)
    List<ReservationJpaEntity> findByRoomIdAndTimeRange(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 檢查會議室在指定時間區間是否有衝突的預約
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param excludeReservationId 排除的預約 ID
     * @return true 如果有衝突
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM ReservationJpaEntity r
        WHERE r.roomId = :roomId
        AND r.startTime < :endTime
        AND r.endTime > :startTime
        AND r.status != 'CANCELLED'
        AND (:excludeReservationId IS NULL OR r.id != :excludeReservationId)
        """)
    boolean hasConflict(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeReservationId") Long excludeReservationId
    );
    
    /**
     * 查詢指定時間區間內即將開始的預約 (用於發送提醒)
     * 
     * @param now 當前時間
     * @param reminderTime 提醒時間
     * @param status 狀態
     * @return List<ReservationJpaEntity>
     */
    @Query("""
        SELECT r FROM ReservationJpaEntity r
        WHERE r.startTime > :now
        AND r.startTime <= :reminderTime
        AND r.status = :status
        """)
    List<ReservationJpaEntity> findUpcomingReservationsForReminder(
            @Param("now") LocalDateTime now,
            @Param("reminderTime") LocalDateTime reminderTime,
            @Param("status") Reservation.ReservationStatus status
    );
    
    /**
     * 查詢使用者的未來預約
     * 
     * @param userId 使用者 ID
     * @param now 當前時間
     * @param status 狀態
     * @return List<ReservationJpaEntity>
     */
    @Query("""
        SELECT r FROM ReservationJpaEntity r
        WHERE r.userId = :userId
        AND r.startTime > :now
        AND r.status = :status
        ORDER BY r.startTime ASC
        """)
    List<ReservationJpaEntity> findUpcomingReservationsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("status") Reservation.ReservationStatus status
    );
    
    /**
     * 查詢使用者的歷史預約
     * 
     * @param userId 使用者 ID
     * @param now 當前時間
     * @return List<ReservationJpaEntity>
     */
    @Query("""
        SELECT r FROM ReservationJpaEntity r
        WHERE r.userId = :userId
        AND r.endTime < :now
        ORDER BY r.endTime DESC
        """)
    List<ReservationJpaEntity> findPastReservationsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );
    
    /**
     * 查詢待審核的預約
     * 
     * @param status 狀態
     * @return List<ReservationJpaEntity>
     */
    List<ReservationJpaEntity> findByStatus(Reservation.ReservationStatus status);
    
    /**
     * 根據外部會議連結查詢預約
     * 
     * @param externalMeetingLink 外部會議連結
     * @return Optional<ReservationJpaEntity>
     */
    Optional<ReservationJpaEntity> findByExternalMeetingLink(String externalMeetingLink);
}
