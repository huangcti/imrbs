package tw.huangcti.imrbs.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.UserRepository;

import java.util.Optional;

/**
 * SsoIntegrationService - SSO 整合服務
 * 
 * 功能:
 * - 處理 SSO 登入後的使用者同步
 * - 從 OIDC UserInfo 提取使用者資訊
 * - 同步使用者資料到本地資料庫
 * - 管理 OAuth 2.0 Token 生命週期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsoIntegrationService {
    
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    
    @Value("${app.security.sso.auto-create-user:true}")
    private boolean autoCreateUser;
    
    /**
     * 處理 SSO 登入成功事件
     * 同步或建立使用者資料
     */
    public User handleSsoLogin(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String employeeId = extractEmployeeId(oidcUser);
        String name = oidcUser.getFullName();
        String department = extractDepartment(oidcUser);
        
        log.info("SSO Login: email={}, employeeId={}, name={}", email, employeeId, name);
        
        // 查找現有使用者
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            // 更新使用者資訊 (可能從 SSO 更新了姓名或部門)
            User user = existingUser.get();
            boolean updated = false;
            
            if (!name.equals(user.getName())) {
                user = user.toBuilder().name(name).build();
                updated = true;
            }
            
            if (department != null && !department.equals(user.getDepartment())) {
                user = user.toBuilder().department(department).build();
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
                log.info("Updated user from SSO: {}", email);
            }
            
            return user;
        } else if (autoCreateUser) {
            // 自動建立新使用者
            User newUser = User.builder()
                    .employeeId(employeeId)
                    .name(name)
                    .email(email)
                    .department(department)
                    .role(User.Role.EMPLOYEE) // 預設角色
                    .isActive(true)
                    .build();
            
            newUser = userRepository.save(newUser);
            log.info("Created new user from SSO: {}", email);
            
            return newUser;
        } else {
            // 不自動建立使用者, 拋出異常
            throw new IllegalStateException("User not found and auto-create is disabled: " + email);
        }
    }
    
    /**
     * 從 OIDC User 提取員工編號
     */
    private String extractEmployeeId(OidcUser oidcUser) {
        // 優先從 preferred_username 取得
        String preferredUsername = oidcUser.getPreferredUsername();
        if (preferredUsername != null) {
            return preferredUsername;
        }
        
        // 次要從 sub (subject) 取得
        String subject = oidcUser.getSubject();
        if (subject != null) {
            return subject;
        }
        
        // 最後從 email 前綴取得
        String email = oidcUser.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        
        throw new IllegalArgumentException("Cannot extract employee ID from OIDC user");
    }
    
    /**
     * 從 OIDC User 提取部門資訊
     */
    private String extractDepartment(OidcUser oidcUser) {
        // 嘗試從 custom claim "department" 取得
        Object department = oidcUser.getAttribute("department");
        if (department instanceof String) {
            return (String) department;
        }
        
        // 嘗試從 "ou" (organizational unit) 取得
        Object ou = oidcUser.getAttribute("ou");
        if (ou instanceof String) {
            return (String) ou;
        }
        
        // 預設值
        return "Unknown";
    }
    
    /**
     * 取得使用者的 OAuth 2.0 Access Token
     */
    public Optional<String> getAccessToken(String principalName, String registrationId) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                registrationId, principalName
        );
        
        if (client != null) {
            OAuth2AccessToken accessToken = client.getAccessToken();
            return Optional.of(accessToken.getTokenValue());
        }
        
        return Optional.empty();
    }
    
    /**
     * 檢查 Access Token 是否即將過期 (5 分鐘內)
     */
    public boolean isTokenExpiringSoon(String principalName, String registrationId) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                registrationId, principalName
        );
        
        if (client != null) {
            OAuth2AccessToken accessToken = client.getAccessToken();
            if (accessToken.getExpiresAt() != null) {
                long expiresInSeconds = accessToken.getExpiresAt().getEpochSecond() - 
                        System.currentTimeMillis() / 1000;
                return expiresInSeconds < 300; // 5 分鐘 = 300 秒
            }
        }
        
        return true; // 無法取得過期時間, 保守判定為即將過期
    }
}
