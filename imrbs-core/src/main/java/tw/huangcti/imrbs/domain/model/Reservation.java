package tw.huangcti.imrbs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reservation 領域模型
 * 
 * 描述: 會議室預約記錄
 * 
 * 業務規則:
 * - endTime 必須 > startTime
 * - 唯一性約束: 同一 roomId，時間區間 [startTime, endTime) 不可重疊 (排除 status = CANCELLED)
 * - status = PENDING 用於訪客預約 (需管理員審核)
 * - 取消預約需在會議開始前 24 小時
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    
    /**
     * 主鍵 (自增)
     */
    private Long id;
    
    /**
     * 會議室 ID (外鍵)
     */
    private Long roomId;
    
    /**
     * 預約者 ID (外鍵)
     */
    private Long userId;
    
    /**
     * 會議主題
     */
    private String meetingTitle;
    
    /**
     * 開始時間
     */
    private LocalDateTime startTime;
    
    /**
     * 結束時間
     */
    private LocalDateTime endTime;
    
    /**
     * 參與者 Email 清單 (逗號分隔)
     */
    private String participants;
    
    /**
     * 會議描述
     */
    private String description;
    
    /**
     * 狀態: PENDING, CONFIRMED, CANCELLED
     */
    private ReservationStatus status;
    
    /**
     * 取消原因
     */
    private String cancellationReason;
    
    /**
     * 外部會議連結 (Teams/Zoom)
     */
    private String externalMeetingLink;
    
    /**
     * 是否為週期性預約
     */
    @Builder.Default
    private Boolean isRecurring = false;
    
    /**
     * 週期規則
     * 結構: {"frequency": "weekly", "days": ["MON", "WED"], "endDate": "2025-12-31"}
     */
    private RecurringRule recurringRule;
    
    /**
     * 創建時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
    
    /**
     * 取消時間
     */
    private LocalDateTime cancelledAt;
    
    /**
     * 預約狀態枚舉
     */
    public enum ReservationStatus {
        /**
         * 待審核 (訪客預約)
         */
        PENDING,
        
        /**
         * 已確認
         */
        CONFIRMED,
        
        /**
         * 已取消
         */
        CANCELLED
    }
    
    /**
     * 週期規則資料結構
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringRule {
        /**
         * 頻率: daily, weekly, monthly
         */
        private String frequency;
        
        /**
         * 重複的星期幾 (週期為 weekly 時使用)
         * 例如: ["MON", "WED", "FRI"]
         */
        @Builder.Default
        private List<String> days = new ArrayList<>();
        
        /**
         * 結束日期 (週期性預約的結束日期)
         */
        private LocalDateTime endDate;
        
        /**
         * 間隔 (每 N 天/週/月)
         */
        private Integer interval;
    }
    
    /**
     * 業務方法: 計算預約時長 (分鐘)
     * 
     * @return 預約時長 (分鐘)
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }
    
    /**
     * 業務方法: 檢查預約是否已確認
     * 
     * @return true 如果狀態為 CONFIRMED
     */
    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }
    
    /**
     * 業務方法: 檢查預約是否已取消
     * 
     * @return true 如果狀態為 CANCELLED
     */
    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }
    
    /**
     * 業務方法: 檢查預約是否待審核
     * 
     * @return true 如果狀態為 PENDING
     */
    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }
    
    /**
     * 業務方法: 檢查預約是否可以取消
     * 規則: 必須在會議開始前 24 小時
     * 
     * @return true 如果當前時間距離開始時間 >= 24 小時
     */
    public boolean canBeCancelled() {
        if (isCancelled()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelDeadline = startTime.minusHours(24);
        
        return now.isBefore(cancelDeadline);
    }
    
    /**
     * 業務方法: 檢查預約是否可以修改
     * 規則: 必須在會議開始前 24 小時，且狀態為 CONFIRMED
     * 
     * @return true 如果可以修改
     */
    public boolean canBeModified() {
        if (!isConfirmed()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime modifyDeadline = startTime.minusHours(24);
        
        return now.isBefore(modifyDeadline);
    }
    
    /**
     * 業務方法: 檢查預約是否與指定時間區間重疊
     * 
     * @param otherStartTime 其他預約的開始時間
     * @param otherEndTime 其他預約的結束時間
     * @return true 如果時間重疊
     */
    public boolean overlapsWith(LocalDateTime otherStartTime, LocalDateTime otherEndTime) {
        // 已取消的預約不參與衝突檢測
        if (isCancelled()) {
            return false;
        }
        
        // 檢查時間區間是否重疊: [startTime, endTime) 與 [otherStartTime, otherEndTime)
        return startTime.isBefore(otherEndTime) && endTime.isAfter(otherStartTime);
    }
    
    /**
     * 業務方法: 檢查預約是否即將開始 (30 分鐘內)
     * 
     * @return true 如果預約在 30 分鐘內開始
     */
    public boolean isStartingSoon() {
        if (!isConfirmed()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = startTime.minusMinutes(30);
        
        return now.isAfter(reminderTime) && now.isBefore(startTime);
    }
    
    /**
     * 業務方法: 取消預約
     * 
     * @param reason 取消原因
     * @throws IllegalStateException 如果預約無法取消
     */
    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("預約無法取消: 必須在會議開始前 24 小時");
        }
        
        this.status = ReservationStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 業務方法: 解析參與者 Email 清單
     * 
     * @return Email 清單
     */
    public List<String> getParticipantEmails() {
        if (participants == null || participants.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return List.of(participants.split(","))
                .stream()
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }
}
