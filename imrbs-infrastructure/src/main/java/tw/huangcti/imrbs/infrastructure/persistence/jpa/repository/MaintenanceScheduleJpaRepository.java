package tw.huangcti.imrbs.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.MaintenanceScheduleJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MaintenanceScheduleJpaRepository - MaintenanceSchedule 的 Spring Data JPA Repository
 */
@Repository
public interface MaintenanceScheduleJpaRepository extends JpaRepository<MaintenanceScheduleJpaEntity, Long> {
    
    List<MaintenanceScheduleJpaEntity> findByRoomId(Long roomId);
    
    @Query("SELECT m FROM MaintenanceScheduleJpaEntity m " +
           "WHERE m.roomId = :roomId " +
           "AND m.startTime < :endTime " +
           "AND m.endTime > :startTime")
    List<MaintenanceScheduleJpaEntity> findByRoomIdAndTimeRange(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT m FROM MaintenanceScheduleJpaEntity m " +
           "WHERE m.startTime >= :fromDate " +
           "ORDER BY m.startTime ASC")
    List<MaintenanceScheduleJpaEntity> findUpcomingMaintenances(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM MaintenanceScheduleJpaEntity m " +
           "WHERE m.roomId = :roomId " +
           "AND m.startTime < :endTime " +
           "AND m.endTime > :startTime " +
           "AND (:excludeScheduleId IS NULL OR m.id != :excludeScheduleId)")
    boolean hasConflict(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeScheduleId") Long excludeScheduleId);
}
