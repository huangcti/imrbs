package tw.huangcti.imrbs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * GuestReservationRequest 領域模型
 * 
 * 描述: 外部訪客提交的預約申請 (需審核)
 * 
 * 業務規則:
 * - 訪客提交申請時，status = PENDING
 * - 管理員批准後，創建 Reservation 記錄，並更新 reservationId
 * - 管理員拒絕時，必須填寫 rejectionReason
 * - 審核後發送通知給訪客 (類型: GUEST_APPROVAL_RESULT)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestReservationRequest {
    
    /**
     * 主鍵 (自增)
     */
    private Long id;
    
    /**
     * 訪客姓名
     */
    private String guestName;
    
    /**
     * 訪客公司
     */
    private String guestCompany;
    
    /**
     * 訪客 Email
     */
    private String guestEmail;
    
    /**
     * 訪客電話
     */
    private String guestPhone;
    
    /**
     * 申請的會議室 ID (外鍵)
     */
    private Long roomId;
    
    /**
     * 期望開始時間
     */
    private LocalDateTime requestedStartTime;
    
    /**
     * 期望結束時間
     */
    private LocalDateTime requestedEndTime;
    
    /**
     * 會議主題
     */
    private String meetingTitle;
    
    /**
     * 會議目的
     */
    private String meetingPurpose;
    
    /**
     * 狀態: PENDING, APPROVED, REJECTED
     */
    private RequestStatus status;
    
    /**
     * 審核者 ID (外鍵)
     */
    private Long reviewedBy;
    
    /**
     * 審核時間
     */
    private LocalDateTime reviewedAt;
    
    /**
     * 拒絕原因
     */
    private String rejectionReason;
    
    /**
     * 批准後創建的預約 ID (外鍵)
     */
    private Long reservationId;
    
    /**
     * 創建時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
    
    /**
     * 申請狀態枚舉
     */
    public enum RequestStatus {
        /**
         * 待審核
         */
        PENDING,
        
        /**
         * 已批准
         */
        APPROVED,
        
        /**
         * 已拒絕
         */
        REJECTED
    }
    
    /**
     * 業務方法: 檢查申請是否待審核
     * 
     * @return true 如果狀態為 PENDING
     */
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
    
    /**
     * 業務方法: 檢查申請是否已批准
     * 
     * @return true 如果狀態為 APPROVED
     */
    public boolean isApproved() {
        return status == RequestStatus.APPROVED;
    }
    
    /**
     * 業務方法: 檢查申請是否已拒絕
     * 
     * @return true 如果狀態為 REJECTED
     */
    public boolean isRejected() {
        return status == RequestStatus.REJECTED;
    }
    
    /**
     * 業務方法: 檢查申請是否已審核 (批准或拒絕)
     * 
     * @return true 如果已審核
     */
    public boolean isReviewed() {
        return isApproved() || isRejected();
    }
    
    /**
     * 業務方法: 批准申請
     * 
     * @param reviewerId 審核者 ID
     * @param createdReservationId 創建的預約 ID
     * @throws IllegalStateException 如果申請不是待審核狀態
     */
    public void approve(Long reviewerId, Long createdReservationId) {
        if (!isPending()) {
            throw new IllegalStateException("只有待審核的申請可以被批准");
        }
        
        if (reviewerId == null) {
            throw new IllegalArgumentException("審核者 ID 不可為空");
        }
        
        if (createdReservationId == null) {
            throw new IllegalArgumentException("預約 ID 不可為空");
        }
        
        this.status = RequestStatus.APPROVED;
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.reservationId = createdReservationId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 業務方法: 拒絕申請
     * 
     * @param reviewerId 審核者 ID
     * @param reason 拒絕原因
     * @throws IllegalStateException 如果申請不是待審核狀態
     * @throws IllegalArgumentException 如果拒絕原因為空
     */
    public void reject(Long reviewerId, String reason) {
        if (!isPending()) {
            throw new IllegalStateException("只有待審核的申請可以被拒絕");
        }
        
        if (reviewerId == null) {
            throw new IllegalArgumentException("審核者 ID 不可為空");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("拒絕原因不可為空");
        }
        
        this.status = RequestStatus.REJECTED;
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 業務方法: 檢查申請時間是否有效
     * 
     * @throws IllegalArgumentException 如果時間區間無效
     */
    public void validateTimeRange() {
        if (requestedStartTime == null || requestedEndTime == null) {
            throw new IllegalArgumentException("開始時間和結束時間不可為空");
        }
        
        if (!requestedEndTime.isAfter(requestedStartTime)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }
    }
    
    /**
     * 業務方法: 檢查申請是否即將過期 (申請時間已過)
     * 
     * @return true 如果申請的開始時間已過且申請仍待審核
     */
    public boolean isExpired() {
        if (!isPending()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(requestedStartTime);
    }
}
