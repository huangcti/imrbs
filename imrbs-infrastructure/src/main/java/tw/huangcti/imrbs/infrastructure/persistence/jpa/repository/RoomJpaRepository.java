package tw.huangcti.imrbs.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.RoomJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RoomJpaRepository - Spring Data JPA Repository
 * 
 * 描述: Room 的 Spring Data JPA 實作
 * 
 * 設計: Infrastructure Layer - 實作 Domain Layer 的 RoomRepository 介面
 */
@Repository
public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long> {
    
    /**
     * 根據名稱查詢會議室
     * 
     * @param name 會議室名稱
     * @return Optional<RoomJpaEntity>
     */
    Optional<RoomJpaEntity> findByName(String name);
    
    /**
     * 查詢所有可預約的會議室 (status = AVAILABLE)
     * 
     * @param status 會議室狀態
     * @return List<RoomJpaEntity>
     */
    List<RoomJpaEntity> findByStatus(Room.RoomStatus status);
    
    /**
     * 根據容量查詢會議室 (容量 >= minCapacity)
     * 
     * @param minCapacity 最小容量
     * @return List<RoomJpaEntity>
     */
    List<RoomJpaEntity> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * 根據建築和樓層查詢會議室
     * 
     * @param building 建築名稱
     * @param floor 樓層
     * @return List<RoomJpaEntity>
     */
    List<RoomJpaEntity> findByBuildingAndFloor(String building, String floor);
    
    /**
     * 查詢指定時間區間內可用的會議室
     * 排除: 已有預約、維護中的會議室
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<RoomJpaEntity>
     */
    @Query("""
        SELECT DISTINCT r FROM RoomJpaEntity r
        WHERE r.status = 'AVAILABLE'
        AND r.id NOT IN (
            SELECT res.roomId FROM ReservationJpaEntity res
            WHERE res.status != 'CANCELLED'
            AND res.startTime < :endTime
            AND res.endTime > :startTime
        )
        AND r.id NOT IN (
            SELECT m.roomId FROM MaintenanceScheduleJpaEntity m
            WHERE m.startTime < :endTime
            AND m.endTime > :startTime
        )
        """)
    List<RoomJpaEntity> findAvailableRooms(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 檢查會議室名稱是否已存在
     * 
     * @param name 會議室名稱
     * @return true 如果已存在
     */
    boolean existsByName(String name);
}
