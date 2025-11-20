package tw.huangcti.imrbs.web.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.Collection;

/**
 * RoleBasedAccessControl - RBAC 權限控制
 * 
 * 定義自訂權限註解與權限檢查邏輯
 * 
 * 角色層級:
 * - EMPLOYEE: 基本員工權限
 * - ROOM_ADMIN: 會議室管理員 (可管理會議室、批准訪客預約)
 * - SYSTEM_ADMIN: 系統管理員 (完整權限)
 */
@Slf4j
@Component("rbac")
public class RoleBasedAccessControl {
    
    /**
     * 檢查當前使用者是否為系統管理員
     */
    public boolean isSystemAdmin() {
        return hasRole("SYSTEM_ADMIN");
    }
    
    /**
     * 檢查當前使用者是否為會議室管理員或更高權限
     */
    public boolean isRoomAdmin() {
        return hasRole("ROOM_ADMIN") || hasRole("SYSTEM_ADMIN");
    }
    
    /**
     * 檢查當前使用者是否為一般員工或更高權限
     */
    public boolean isEmployee() {
        return hasRole("EMPLOYEE") || hasRole("ROOM_ADMIN") || hasRole("SYSTEM_ADMIN");
    }
    
    /**
     * 檢查當前使用者是否具有指定角色
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleWithPrefix = "ROLE_" + role;
        
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }
    
    /**
     * 檢查當前使用者是否為預約擁有者
     * 
     * @param resourceOwnerId 資源擁有者的使用者 ID 或 email
     */
    public boolean isOwner(String resourceOwnerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String currentUser = authentication.getName();
        return currentUser.equals(resourceOwnerId);
    }
    
    /**
     * 檢查當前使用者是否為資源擁有者或管理員
     */
    public boolean isOwnerOrAdmin(String resourceOwnerId) {
        return isOwner(resourceOwnerId) || isRoomAdmin();
    }
}

/**
 * 自訂註解: 僅員工可存取
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('EMPLOYEE')")
@interface RequireEmployee {
}

/**
 * 自訂註解: 僅會議室管理員可存取
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('ROOM_ADMIN', 'SYSTEM_ADMIN')")
@interface RequireRoomAdmin {
}

/**
 * 自訂註解: 僅系統管理員可存取
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@interface RequireSystemAdmin {
}

/**
 * 自訂註解: 資源擁有者或管理員可存取
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@rbac.isOwnerOrAdmin(#resourceOwnerId)")
@interface RequireOwnerOrAdmin {
}
