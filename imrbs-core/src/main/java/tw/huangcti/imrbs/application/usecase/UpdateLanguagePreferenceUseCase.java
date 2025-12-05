package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * T167 [P] [US8] 更新語言偏好 Use Case
 * 
 * 功能:
 * - 更新使用者的語言偏好設定 (zh-TW, en)
 * - 驗證語言代碼是否支援
 * - 儲存語言設定至資料庫
 * 
 * 支援語言:
 * - zh-TW: 繁體中文
 * - en: 英文
 * 
 * @author IMRBS Team
 * @since 2025-01-24
 */
@RequiredArgsConstructor
public class UpdateLanguagePreferenceUseCase {

    private final UserRepository userRepository;

    /**
     * 支援的語言代碼
     */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("zh-TW", "en");

    /**
     * 執行語言偏好更新
     * 
     * @param employeeId 員工編號 (來自認證)
     * @param language   目標語言代碼 (zh-TW, en)
     * @return 更新後的 User 實體
     * @throws NotFoundException   使用者不存在
     * @throws ValidationException 語言代碼不支援
     */
    public User execute(String employeeId, String language) {
        // 驗證語言代碼
        validateLanguage(language);

        // 查詢使用者
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("找不到使用者: " + employeeId));

        // 更新語言偏好
        user.setLanguagePreference(normalizeLanguage(language));
        user.setUpdatedAt(LocalDateTime.now());

        // 儲存並返回
        return userRepository.save(user);
    }

    /**
     * 驗證語言代碼是否支援
     * 
     * @param language 語言代碼
     * @throws ValidationException 如果語言不支援
     */
    private void validateLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new ValidationException("語言代碼不可為空");
        }

        String normalized = normalizeLanguage(language);
        if (!SUPPORTED_LANGUAGES.contains(normalized)) {
            throw new ValidationException("不支援的語言: " + language);
        }
    }

    /**
     * 標準化語言代碼
     * 支援多種格式: zh-TW, zh_TW, zh-Hant, en, en-US, en-GB
     * 
     * @param language 原始語言代碼
     * @return 標準化語言代碼 (zh-TW 或 en)
     */
    private String normalizeLanguage(String language) {
        if (language == null) {
            return "zh-TW"; // 預設繁體中文
        }

        String lower = language.toLowerCase().trim();

        // 繁體中文變體
        if (lower.startsWith("zh-tw") || lower.startsWith("zh_tw") ||
            lower.equals("zh-hant") || lower.equals("zh_hant")) {
            return "zh-TW";
        }

        // 簡體中文變體 -> 預設繁體中文
        if (lower.startsWith("zh-cn") || lower.startsWith("zh_cn") ||
            lower.equals("zh-hans") || lower.equals("zh_hans")) {
            return "zh-TW";
        }

        // 英文變體
        if (lower.equals("en") || lower.startsWith("en-") || lower.startsWith("en_")) {
            return "en";
        }

        // 不支援的語言返回原值 (將在驗證階段拋出異常)
        return language;
    }

    /**
     * 檢查是否為支援的語言
     * 
     * @param language 語言代碼
     * @return true 如果支援
     */
    public static boolean isSupported(String language) {
        if (language == null || language.isBlank()) {
            return false;
        }
        
        String lower = language.toLowerCase().trim();
        return lower.startsWith("zh-tw") || lower.startsWith("zh_tw") ||
               lower.equals("zh-hant") || lower.equals("zh_hant") ||
               lower.equals("en") || lower.startsWith("en-") || lower.startsWith("en_");
    }
}
