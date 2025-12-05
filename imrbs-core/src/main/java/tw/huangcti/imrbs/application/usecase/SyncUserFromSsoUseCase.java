package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SyncUserFromSsoUseCase - 從 SSO 同步使用者資料
 * 
 * 功能:
 * - 接收已解析的 SSO 使用者資訊
 * - 檢查本地資料庫是否已存在該使用者
 * - 若不存在則創建新使用者
 * - 若已存在則更新使用者資訊 (姓名、Email、最後登入時間)
 * 
 * 使用場景:
 * - 使用者首次透過 SSO 登入 (創建新帳號)
 * - 使用者再次登入 (更新個人資訊)
 * 
 * 設計原則:
 * - Core 層不依賴 JWT 庫 (框架無關)
 * - 由 Web 層解析 ID Token 後傳入 SsoUserInfo
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */
@RequiredArgsConstructor
public class SyncUserFromSsoUseCase {

    private final UserRepository userRepository;

    /**
     * SsoUserInfo - SSO 使用者資訊 DTO
     */
    public record SsoUserInfo(
            String employeeId,
            String email,
            String fullName,
            String department,
            String role
    ) {
        public SsoUserInfo {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                throw new IllegalArgumentException("employeeId 不可為空");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("email 不可為空");
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new IllegalArgumentException("fullName 不可為空");
            }
        }
    }

    /**
     * 執行 SSO 使用者同步
     * 
     * @param ssoUserInfo SSO 使用者資訊 (已從 ID Token 解析)
     * @return 同步後的 User 實體
     */
    public User execute(SsoUserInfo ssoUserInfo) {
        // 映射 SSO 角色到系統角色
        User.UserRole role = mapSsoRoleToUserRole(ssoUserInfo.role());

        // 檢查使用者是否已存在
        Optional<User> existingUser = userRepository.findByEmployeeId(ssoUserInfo.employeeId());

        User user;
        if (existingUser.isPresent()) {
            // 更新現有使用者
            user = updateExistingUser(
                    existingUser.get(),
                    ssoUserInfo.fullName(),
                    ssoUserInfo.email(),
                    ssoUserInfo.department(),
                    role
            );
        } else {
            // 創建新使用者
            user = createNewUser(
                    ssoUserInfo.employeeId(),
                    ssoUserInfo.email(),
                    ssoUserInfo.fullName(),
                    ssoUserInfo.department(),
                    role
            );
        }

        return userRepository.save(user);
    }

    /**
     * 映射 SSO 角色到系統角色
     * 
     * SSO Roles 格式: "EMPLOYEE", "ROOM_ADMIN", "SYSTEM_ADMIN"
     * 預設角色: EMPLOYEE
     * 
     * @param ssoRole SSO 提供的角色字串
     * @return User.UserRole 系統角色列舉
     */
    private User.UserRole mapSsoRoleToUserRole(String ssoRole) {
        if (ssoRole == null || ssoRole.trim().isEmpty()) {
            return User.UserRole.EMPLOYEE; // 預設角色
        }

        // 移除可能的前綴 (如 "ROLE_")
        String normalizedRole = ssoRole.replace("ROLE_", "").trim().toUpperCase();

        return switch (normalizedRole) {
            case "SYSTEM_ADMIN" -> User.UserRole.SYSTEM_ADMIN;
            case "ROOM_ADMIN" -> User.UserRole.ROOM_ADMIN;
            default -> User.UserRole.EMPLOYEE;
        };
    }

    /**
     * 更新現有使用者資訊
     */
    private User updateExistingUser(User user, String fullName, String email, String department, User.UserRole role) {
        user.setFullName(fullName);
        user.setEmail(email);
        user.setDepartment(department);
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 確保帳號為啟用狀態 (可能之前被軟刪除)
        if (Boolean.FALSE.equals(user.getIsActive())) {
            user.setIsActive(true);
        }
        
        return user;
    }

    /**
     * 創建新使用者
     */
    private User createNewUser(String employeeId, String email, String fullName, String department, User.UserRole role) {
        LocalDateTime now = LocalDateTime.now();
        
        return User.builder()
                .employeeId(employeeId)
                .email(email)
                .fullName(fullName)
                .department(department != null ? department : "未指定部門")
                .role(role)
                .languagePreference("zh-TW") // 預設繁體中文
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
