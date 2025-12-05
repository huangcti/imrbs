package tw.huangcti.imrbs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Room 領域模型
 * 
 * 描述: 可預約的會議室資源
 * 
 * 業務規則:
 * - name 必須唯一 (同一組織內不可重複)
 * - capacity 必須 > 0
 * - status = MAINTENANCE 時，該會議室不可預約
 * - 軟刪除: status = DISABLED (保留歷史預約記錄)
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    /**
     * 主鍵 (自增)
     */
    private Long id;
    
    /**
     * 會議室名稱 (必須唯一)
     */
    private String name;
    
    /**
     * 樓層 (如 "3F", "B1")
     */
    private String floor;
    
    /**
     * 建築名稱 (多辦公室場景)
     */
    private String building;
    
    /**
     * 位置描述 (如 "電梯旁")
     */
    private String locationDescription;
    
    /**
     * 容納人數
     */
    private Integer capacity;
    
    /**
     * 設備清單 (JSON 格式)
     * 結構: [{"name": "投影機", "quantity": 1, "brand": "..."}]
     */
    @Builder.Default
    private List<Equipment> equipment = new ArrayList<>();
    
    /**
     * 照片 URL 清單
     */
    @Builder.Default
    private List<String> photos = new ArrayList<>();
    
    /**
     * 會議室狀態: AVAILABLE, MAINTENANCE, DISABLED
     */
    private RoomStatus status;
    
    /**
     * 特色標籤 (如 ["video_conferencing", "whiteboard"])
     */
    @Builder.Default
    private List<String> features = new ArrayList<>();
    
    /**
     * 預約規則
     * 結構: {"max_duration_hours": 4, "advance_booking_days": 30, ...}
     */
    private BookingRule bookingRule;
    
    /**
     * 創建時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
    
    /**
     * 會議室狀態枚舉
     */
    public enum RoomStatus {
        /**
         * 可預約
         */
        AVAILABLE,
        
        /**
         * 維護中 (不可預約)
         */
        MAINTENANCE,
        
        /**
         * 已停用 (軟刪除)
         */
        DISABLED
    }
    
    /**
     * 設備資料結構
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Equipment {
        /**
         * 設備名稱
         */
        private String name;
        
        /**
         * 數量
         */
        private Integer quantity;
        
        /**
         * 品牌 (可選)
         */
        private String brand;
    }
    
    /**
     * 預約規則資料結構
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingRule {
        /**
         * 最大預約時長 (小時)
         */
        private Integer maxDurationHours;
        
        /**
         * 可提前預約天數
         */
        private Integer advanceBookingDays;
        
        /**
         * 最小預約時長 (分鐘)
         */
        private Integer minBookingMinutes;
        
        /**
         * 可預約時間段 (如 ["09:00", "09:30", "10:00"])
         */
        @Builder.Default
        private List<String> bookingTimeSlots = new ArrayList<>();
    }
    
    /**
     * 業務方法: 檢查會議室是否可預約
     * 
     * @return true 如果狀態為 AVAILABLE
     */
    public boolean isAvailableForBooking() {
        return status == RoomStatus.AVAILABLE;
    }
    
    /**
     * 業務方法: 檢查會議室是否在維護中
     * 
     * @return true 如果狀態為 MAINTENANCE
     */
    public boolean isUnderMaintenance() {
        return status == RoomStatus.MAINTENANCE;
    }
    
    /**
     * 業務方法: 檢查會議室是否已停用
     * 
     * @return true 如果狀態為 DISABLED
     */
    public boolean isDisabled() {
        return status == RoomStatus.DISABLED;
    }
    
    /**
     * 業務方法: 檢查會議室容量是否符合需求
     * 
     * @param requiredCapacity 所需容量
     * @return true 如果會議室容量 >= 所需容量
     */
    public boolean hasCapacityFor(int requiredCapacity) {
        return capacity != null && capacity >= requiredCapacity;
    }
    
    /**
     * 業務方法: 檢查會議室是否具備特定設備
     * 
     * @param equipmentName 設備名稱
     * @return true 如果會議室具備該設備
     */
    public boolean hasEquipment(String equipmentName) {
        return equipment.stream()
                .anyMatch(e -> e.getName().equalsIgnoreCase(equipmentName));
    }
    
    /**
     * 業務方法: 檢查會議室是否具備特定特色
     * 
     * @param feature 特色標籤
     * @return true 如果會議室具備該特色
     */
    public boolean hasFeature(String feature) {
        return features.contains(feature);
    }
}
