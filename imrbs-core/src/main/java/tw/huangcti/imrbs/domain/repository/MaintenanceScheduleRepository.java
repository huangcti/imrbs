package tw.huangcti.imrbs.domain.repository;

import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MaintenanceScheduleRepository 介面
 * 
 * 描述: 維護時程資料存取介面 (Domain Layer)
 * 
 * 實作位置: Infrastructure Layer (imrbs-infrastructure)
 */
public interface MaintenanceScheduleRepository {
    
    /**
     * 根據 ID 查詢維護時程
     * 
     * @param id 維護時程 ID
     * @return Optional<MaintenanceSchedule>
     */
    Optional<MaintenanceSchedule> findById(Long id);
    
    /**
     * 根據會議室 ID 查詢維護時程清單
     * 
     * @param roomId 會議室 ID
     * @return List<MaintenanceSchedule>
     */
    List<MaintenanceSchedule> findByRoomId(Long roomId);
    
    /**
     * 根據會議室 ID 和時間範圍查詢維護時程清單
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<MaintenanceSchedule>
     */
    List<MaintenanceSchedule> findByRoomIdAndTimeRange(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    
    /**
     * 檢查會議室在指定時間區間是否有維護時程
     * 
     * @param roomId 會議室 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return true 如果有維護時程
     */
    boolean hasMaintenanceInTimeRange(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    
    /**
     * 查詢正在進行中的維護時程 (startTime < now < endTime)
     * 
     * @return List<MaintenanceSchedule>
     */
    List<MaintenanceSchedule> findOngoingMaintenance();
    
    /**
     * 查詢即將開始的維護時程 (startTime 在未來 N 小時內)
     * 
     * @param hoursFromNow 幾小時內
     * @return List<MaintenanceSchedule>
     */
    List<MaintenanceSchedule> findUpcomingMaintenance(int hoursFromNow);
    
    /**
     * 根據創建者 ID 查詢維護時程清單
     * 
     * @param createdBy 創建者 ID
     * @return List<MaintenanceSchedule>
     */
    List<MaintenanceSchedule> findByCreatedBy(Long createdBy);
    
    /**
     * 儲存維護時程 (新增或更新)
     * 
     * @param maintenanceSchedule 維護時程實體
     * @return 儲存後的維護時程實體
     */
    MaintenanceSchedule save(MaintenanceSchedule maintenanceSchedule);
    
    /**
     * 刪除維護時程 (硬刪除)
     * 
     * @param id 維護時程 ID
     */
    void deleteById(Long id);
    
    /**
     * 查詢所有維護時程
     * 
     * @return List<MaintenanceSchedule>
     */
    List<MaintenanceSchedule> findAll();
}
