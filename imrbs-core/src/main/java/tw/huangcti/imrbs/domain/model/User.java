package tw.huangcti.imrbs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User 領域模型
 * 
 * 描述: 系統使用者，包含員工與管理員
 * 
 * 業務規則:
 * - employee_id 由 SSO 系統提供，不可修改
 * - email 必須符合 Email 格式
 * - role 預設為 EMPLOYEE，僅 SYSTEM_ADMIN 可修改
 * - 軟刪除: isActive = false (保留歷史記錄)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 主鍵 (自增)
     */
    private Long id;
    
    /**
     * 員工編號 (來自 SSO)，不可修改
     */
    private String employeeId;
    
    /**
     * Email (加密儲存)
     */
    private String email;
    
    /**
     * 全名
     */
    private String fullName;
    
    /**
     * 部門名稱
     */
    private String department;
    
    /**
     * 角色: EMPLOYEE, ROOM_ADMIN, SYSTEM_ADMIN
     */
    private UserRole role;
    
    /**
     * 語言偏好: zh-TW, en
     */
    private String languagePreference;
    
    /**
     * 聯絡電話
     */
    private String phoneNumber;
    
    /**
     * 帳號狀態 (軟刪除標記)
     */
    private Boolean isActive;
    
    /**
     * 創建時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
    
    /**
     * 使用者角色枚舉
     */
    public enum UserRole {
        /**
         * 一般員工 (預設角色)
         */
        EMPLOYEE,
        
        /**
         * 會議室管理員 (可管理會議室與維護時程)
         */
        ROOM_ADMIN,
        
        /**
         * 系統管理員 (最高權限)
         */
        SYSTEM_ADMIN
    }
    
    /**
     * 業務方法: 檢查使用者是否為管理員
     * 
     * @return true 如果使用者為 ROOM_ADMIN 或 SYSTEM_ADMIN
     */
    public boolean isAdmin() {
        return role == UserRole.ROOM_ADMIN || role == UserRole.SYSTEM_ADMIN;
    }
    
    /**
     * 業務方法: 檢查使用者是否為系統管理員
     * 
     * @return true 如果使用者為 SYSTEM_ADMIN
     */
    public boolean isSystemAdmin() {
        return role == UserRole.SYSTEM_ADMIN;
    }
    
    /**
     * 業務方法: 檢查使用者是否可以修改其他使用者的角色
     * 
     * @return true 如果使用者為 SYSTEM_ADMIN
     */
    public boolean canModifyUserRoles() {
        return isSystemAdmin();
    }
    
    /**
     * 業務方法: 檢查使用者是否可以創建維護時程
     * 
     * @return true 如果使用者為 ROOM_ADMIN 或 SYSTEM_ADMIN
     */
    public boolean canCreateMaintenanceSchedule() {
        return isAdmin();
    }
    
    /**
     * 業務方法: 檢查使用者是否可以審核訪客預約申請
     * 
     * @return true 如果使用者為 ROOM_ADMIN 或 SYSTEM_ADMIN
     */
    public boolean canReviewGuestRequests() {
        return isAdmin();
    }
}
