package tw.huangcti.imrbs.domain.repository;

import tw.huangcti.imrbs.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository 介面
 * 
 * 描述: 使用者資料存取介面 (Domain Layer)
 * 
 * 實作位置: Infrastructure Layer (imrbs-infrastructure)
 * 
 * 設計原則: Clean Architecture - Domain 定義介面，Infrastructure 實作
 */
public interface UserRepository {
    
    /**
     * 根據 ID 查詢使用者
     * 
     * @param id 使用者 ID
     * @return Optional<User>
     */
    Optional<User> findById(Long id);
    
    /**
     * 根據使用者名稱查詢使用者 (SSO 登入用)
     * 
     * @param username 使用者名稱
     * @return Optional<User>
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根據員工編號查詢使用者 (SSO 登入用)
     * 
     * @param employeeId 員工編號
     * @return Optional<User>
     */
    Optional<User> findByEmployeeId(String employeeId);
    
    /**
     * 根據 Email 查詢使用者
     * 
     * @param email Email
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根據角色查詢使用者清單
     * 
     * @param role 角色 (EMPLOYEE, ROOM_ADMIN, SYSTEM_ADMIN)
     * @return List<User>
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * 查詢所有啟用中的使用者
     * 
     * @return List<User>
     */
    List<User> findAllActive();
    
    /**
     * 查詢所有管理員 (ROOM_ADMIN + SYSTEM_ADMIN)
     * 
     * @return List<User>
     */
    List<User> findAllAdmins();
    
    /**
     * 儲存使用者 (新增或更新)
     * 
     * @param user 使用者實體
     * @return 儲存後的使用者實體
     */
    User save(User user);
    
    /**
     * 刪除使用者 (軟刪除: isActive = false)
     * 
     * @param id 使用者 ID
     */
    void deleteById(Long id);
    
    /**
     * 檢查員工編號是否已存在
     * 
     * @param employeeId 員工編號
     * @return true 如果已存在
     */
    boolean existsByEmployeeId(String employeeId);
    
    /**
     * 檢查 Email 是否已存在
     * 
     * @param email Email
     * @return true 如果已存在
     */
    boolean existsByEmail(String email);
}
