package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.UserRepository;

/**
 * GetUserProfileUseCase - 取得使用者個人資料
 * 
 * 功能:
 * - 根據員工編號取得使用者完整個人資料
 * - 用於 /api/users/me 端點
 * 
 * @author IMRBS Team
 * @since 2025-01-24
 */
@RequiredArgsConstructor
public class GetUserProfileUseCase {

    private final UserRepository userRepository;

    /**
     * 執行取得使用者個人資料
     * 
     * @param employeeId 員工編號 (來自認證)
     * @return User 使用者實體
     * @throws NotFoundException 使用者不存在
     */
    public User execute(String employeeId) {
        return userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("找不到使用者: " + employeeId));
    }
}
