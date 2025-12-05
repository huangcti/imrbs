package tw.huangcti.imrbs.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.application.usecase.GetUserProfileUseCase;
import tw.huangcti.imrbs.application.usecase.UpdateLanguagePreferenceUseCase;
import tw.huangcti.imrbs.domain.model.User;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/**
 * T168 [P] [US8] UserController - 使用者控制器
 * 
 * 端點:
 * - GET /api/users/me - 取得當前使用者個人資料
 * - PUT /api/users/me/language - 更新語言偏好
 * 
 * @author IMRBS Team
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateLanguagePreferenceUseCase updateLanguagePreferenceUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final MessageSource messageSource;

    /**
     * 更新語言請求 DTO
     */
    public record UpdateLanguageRequest(
            @NotBlank(message = "語言代碼不可為空")
            String language
    ) {}

    /**
     * 使用者資料響應 DTO
     */
    public record UserProfileResponse(
            Long id,
            String employeeId,
            String email,
            String fullName,
            String department,
            String role,
            String languagePreference,
            String phoneNumber,
            Boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static UserProfileResponse from(User user) {
            return new UserProfileResponse(
                    user.getId(),
                    user.getEmployeeId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getDepartment(),
                    user.getRole() != null ? user.getRole().name() : null,
                    user.getLanguagePreference(),
                    user.getPhoneNumber(),
                    user.getIsActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }

    /**
     * GET /api/users/me
     * 
     * 取得當前使用者個人資料
     * 
     * @param jwt 當前使用者的 JWT Token (由 Spring Security 注入)
     * @return 使用者個人資料
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @AuthenticationPrincipal Jwt jwt) {
        
        String employeeId = extractEmployeeId(jwt);
        log.info("取得使用者個人資料: employeeId={}", employeeId);

        User user = getUserProfileUseCase.execute(employeeId);
        UserProfileResponse response = UserProfileResponse.from(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    /**
     * PUT /api/users/me/language
     * 
     * 更新使用者語言偏好
     * 
     * @param jwt     當前使用者的 JWT Token
     * @param request 更新語言請求
     * @return 更新後的使用者資料
     */
    @PutMapping("/me/language")
    public ResponseEntity<Map<String, Object>> updateLanguagePreference(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateLanguageRequest request) {
        
        String employeeId = extractEmployeeId(jwt);
        String language = request.language();
        
        log.info("更新語言偏好: employeeId={}, language={}", employeeId, language);

        User updatedUser = updateLanguagePreferenceUseCase.execute(employeeId, language);
        UserProfileResponse response = UserProfileResponse.from(updatedUser);

        // 根據更新後的語言返回訊息
        Locale locale = parseLocale(language);
        String message = messageSource.getMessage("user.language.updated", null, locale);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response,
                "message", message
        ));
    }

    /**
     * 從 JWT Token 提取員工編號
     * 
     * @param jwt JWT Token
     * @return 員工編號
     */
    private String extractEmployeeId(Jwt jwt) {
        // 優先從 preferred_username claim 取得員工編號
        String employeeId = jwt.getClaimAsString("preferred_username");
        if (employeeId != null && !employeeId.isBlank()) {
            return employeeId;
        }

        // 備選: 從 sub claim 取得
        employeeId = jwt.getClaimAsString("sub");
        if (employeeId != null && !employeeId.isBlank()) {
            return employeeId;
        }

        // 備選: 從 employee_id claim 取得
        return jwt.getClaimAsString("employee_id");
    }

    /**
     * 解析語言偏好設定
     * 
     * @param language 語言代碼
     * @return Locale
     */
    private Locale parseLocale(String language) {
        if (language == null || language.isBlank()) {
            return Locale.TRADITIONAL_CHINESE;
        }

        return switch (language.toLowerCase()) {
            case "en", "en-us", "en-gb" -> Locale.ENGLISH;
            case "zh-tw", "zh-hant", "zh_tw" -> Locale.TRADITIONAL_CHINESE;
            default -> Locale.TRADITIONAL_CHINESE;
        };
    }
}
