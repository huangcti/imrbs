package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;

import java.time.LocalDateTime;

/**
 * 批准訪客預約申請用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排訪客預約申請批准流程，包含驗證、創建預約、更新狀態
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class ApproveGuestRequestUseCase {

    private final GuestReservationRequestRepository guestReservationRequestRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ConflictDetectionService conflictDetectionService;

    /**
     * 執行批准訪客預約申請
     * 
     * @param requestId 訪客預約申請 ID
     * @param approverUsername 批准者的使用者名稱
     * @return 更新後的訪客預約申請
     * @throws NotFoundException 申請不存在
     * @throws IllegalStateException 申請已被處理或時段衝突
     */
    public GuestReservationRequest execute(Long requestId, String approverUsername) {
        // 1. 驗證輸入
        if (requestId == null) {
            throw new ValidationException("申請 ID 不能為空");
        }
        if (approverUsername == null || approverUsername.trim().isEmpty()) {
            throw new ValidationException("批准者不能為空");
        }

        // 2. 查詢訪客預約申請
        GuestReservationRequest request = guestReservationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("申請不存在: " + requestId));

        // 3. 檢查申請狀態
        if (!request.isPending()) {
            throw new IllegalStateException("該申請已被處理");
        }

        // 4. 檢查申請是否過期
        if (request.isExpired()) {
            throw new IllegalStateException("該申請已過期，申請的會議時間已過");
        }

        // 5. 查詢批准者
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new NotFoundException("批准者不存在: " + approverUsername));

        // 6. 查詢會議室
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("會議室不存在: " + request.getRoomId()));

        // 7. 再次檢測時段衝突 (防止審核期間有新的預約)
        boolean hasConflict = conflictDetectionService.hasConflict(
                request.getRoomId(),
                request.getRequestedStartTime(),
                request.getRequestedEndTime(),
                null // 新建預約，無需排除
        );

        if (hasConflict) {
            throw new IllegalStateException("該時段已有新的預約，無法批准");
        }

        // 8. 創建正式預約
        Reservation reservation = Reservation.builder()
                .roomId(request.getRoomId())
                .userId(approver.getId()) // 由批准者代為創建，實際應紀錄訪客信息
                .meetingTitle(request.getMeetingTitle())
                .startTime(request.getRequestedStartTime())
                .endTime(request.getRequestedEndTime())
                .description(buildDescription(request))
                .status(Reservation.ReservationStatus.CONFIRMED)
                .isRecurring(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 9. 更新訪客預約申請狀態
        request.approve(approver.getId(), savedReservation.getId());
        GuestReservationRequest updatedRequest = guestReservationRequestRepository.save(request);

        // 10. 發送通知給訪客 (由事件處理)
        // 實際應使用 Spring Events 或 RabbitMQ

        return updatedRequest;
    }

    /**
     * 建立預約描述
     */
    private String buildDescription(GuestReservationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【訪客預約】\n");
        sb.append("訪客姓名: ").append(request.getGuestName()).append("\n");
        if (request.getGuestCompany() != null && !request.getGuestCompany().isEmpty()) {
            sb.append("訪客公司: ").append(request.getGuestCompany()).append("\n");
        }
        sb.append("訪客 Email: ").append(request.getGuestEmail()).append("\n");
        if (request.getGuestPhone() != null && !request.getGuestPhone().isEmpty()) {
            sb.append("訪客電話: ").append(request.getGuestPhone()).append("\n");
        }
        if (request.getMeetingPurpose() != null && !request.getMeetingPurpose().isEmpty()) {
            sb.append("會議目的: ").append(request.getMeetingPurpose());
        }
        return sb.toString();
    }
}
