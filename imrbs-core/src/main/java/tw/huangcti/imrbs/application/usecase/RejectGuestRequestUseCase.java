package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;

/**
 * 拒絕訪客預約申請用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排訪客預約申請拒絕流程，包含驗證、更新狀態、發送通知
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class RejectGuestRequestUseCase {

    private final GuestReservationRequestRepository guestReservationRequestRepository;
    private final UserRepository userRepository;

    /**
     * 執行拒絕訪客預約申請
     * 
     * @param requestId 訪客預約申請 ID
     * @param rejectorUsername 拒絕者的使用者名稱
     * @param reason 拒絕原因
     * @return 更新後的訪客預約申請
     * @throws NotFoundException 申請不存在
     * @throws ValidationException 驗證失敗
     * @throws IllegalStateException 申請已被處理
     */
    public GuestReservationRequest execute(Long requestId, String rejectorUsername, String reason) {
        // 1. 驗證輸入
        if (requestId == null) {
            throw new ValidationException("申請 ID 不能為空");
        }
        if (rejectorUsername == null || rejectorUsername.trim().isEmpty()) {
            throw new ValidationException("拒絕者不能為空");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("拒絕原因不能為空");
        }

        // 2. 查詢訪客預約申請
        GuestReservationRequest request = guestReservationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("申請不存在: " + requestId));

        // 3. 檢查申請狀態
        if (!request.isPending()) {
            throw new IllegalStateException("該申請已被處理");
        }

        // 4. 查詢拒絕者
        User rejector = userRepository.findByUsername(rejectorUsername)
                .orElseThrow(() -> new NotFoundException("拒絕者不存在: " + rejectorUsername));

        // 5. 更新訪客預約申請狀態
        request.reject(rejector.getId(), reason);
        GuestReservationRequest updatedRequest = guestReservationRequestRepository.save(request);

        // 6. 發送通知給訪客 (由事件處理)
        // 實際應使用 Spring Events 或 RabbitMQ

        return updatedRequest;
    }
}
