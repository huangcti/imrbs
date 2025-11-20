package tw.huangcti.imrbs.domain.repository;

import tw.huangcti.imrbs.domain.model.Room;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RoomRepository 介面
 * 
 * 描述: 會議室資料存取介面 (Domain Layer)
 * 
 * 實作位置: Infrastructure Layer (imrbs-infrastructure)
 */
public interface RoomRepository {
    
    /**
     * 根據 ID 查詢會議室
     * 
     * @param id 會議室 ID
     * @return Optional<Room>
     */
    Optional<Room> findById(Long id);
    
    /**
     * 根據名稱查詢會議室
     * 
     * @param name 會議室名稱
     * @return Optional<Room>
     */
    Optional<Room> findByName(String name);
    
    /**
     * 查詢所有可預約的會議室 (status = AVAILABLE)
     * 
     * @return List<Room>
     */
    List<Room> findAllAvailable();
    
    /**
     * 根據容量查詢會議室 (容量 >= minCapacity)
     * 
     * @param minCapacity 最小容量
     * @return List<Room>
     */
    List<Room> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * 根據建築和樓層查詢會議室
     * 
     * @param building 建築名稱
     * @param floor 樓層
     * @return List<Room>
     */
    List<Room> findByBuildingAndFloor(String building, String floor);
    
    /**
     * 查詢指定時間區間內可用的會議室
     * 排除: 已有預約、維護中的會議室
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<Room>
     */
    List<Room> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查詢符合條件的可用會議室
     * 條件: 時間區間、最小容量、設備需求
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param minCapacity 最小容量
     * @param requiredEquipment 必備設備清單 (可為空)
     * @return List<Room>
     */
    List<Room> findAvailableRoomsByFilters(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer minCapacity,
            List<String> requiredEquipment
    );
    
    /**
     * 根據特色標籤查詢會議室
     * 
     * @param feature 特色標籤 (如 "video_conferencing")
     * @return List<Room>
     */
    List<Room> findByFeature(String feature);
    
    /**
     * 儲存會議室 (新增或更新)
     * 
     * @param room 會議室實體
     * @return 儲存後的會議室實體
     */
    Room save(Room room);
    
    /**
     * 刪除會議室 (軟刪除: status = DISABLED)
     * 
     * @param id 會議室 ID
     */
    void deleteById(Long id);
    
    /**
     * 檢查會議室名稱是否已存在
     * 
     * @param name 會議室名稱
     * @return true 如果已存在
     */
    boolean existsByName(String name);
    
    /**
     * 查詢所有會議室 (包含已停用)
     * 
     * @return List<Room>
     */
    List<Room> findAll();
}
