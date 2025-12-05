package tw.huangcti.imrbs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MaintenanceSchedule 領域模型
 * 
 * 描述: 會議室維護時段記錄
 * 
 * 業務規則:
 * - endTime 必須 > startTime
 * - 維護期間，該會議室不可預約
 * - 僅 ROOM_ADMIN 與 SYSTEM_ADMIN 可創建維護時程
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceSchedule {
    
    /**
     * 主鍵 (自增)
     */
    private Long id;
    
    /**
     * 會議室 ID (外鍵)
     */
    private Long roomId;
    
    /**
     * 開始時間
     */
    private LocalDateTime startTime;
    
    /**
     * 結束時間
     */
    private LocalDateTime endTime;
    
    /**
     * 維護原因
     */
    private String reason;
    
    /**
     * 創建者 ID (外鍵，必須為管理員)
     */
    private Long createdBy;
    
    /**
     * 備註
     */
    private String notes;
    
    /**
     * 創建時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 業務方法: 檢查維護時程是否與指定時間區間重疊
     * 
     * @param checkStartTime 要檢查的開始時間
     * @param checkEndTime 要檢查的結束時間
     * @return true 如果時間重疊
     */
    public boolean overlapsWith(LocalDateTime checkStartTime, LocalDateTime checkEndTime) {
        // 檢查時間區間是否重疊: [startTime, endTime) 與 [checkStartTime, checkEndTime)
        return startTime.isBefore(checkEndTime) && endTime.isAfter(checkStartTime);
    }
    
    /**
     * 業務方法: 檢查維護時程是否正在進行中
     * 
     * @return true 如果當前時間在維護時段內
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
    
    /**
     * 業務方法: 檢查維護時程是否即將開始 (1 小時內)
     * 
     * @return true 如果維護將在 1 小時內開始
     */
    public boolean isStartingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);
        
        return startTime.isAfter(now) && startTime.isBefore(oneHourFromNow);
    }
    
    /**
     * 業務方法: 檢查維護時程是否已結束
     * 
     * @return true 如果維護已結束
     */
    public boolean isCompleted() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(endTime);
    }
    
    /**
     * 業務方法: 檢查維護時程的有效性
     * 
     * @throws IllegalArgumentException 如果時間區間無效
     */
    public void validate() {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("開始時間和結束時間不可為空");
        }
        
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("維護原因不可為空");
        }
    }
}
