package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;

import java.time.LocalDateTime;

/**
 * 創建訪客預約申請用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排訪客預約申請創建流程，包含驗證、衝突檢測、儲存
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class CreateGuestRequestUseCase {

    private final RoomRepository roomRepository;
    private final GuestReservationRequestRepository guestReservationRequestRepository;
    private final ConflictDetectionService conflictDetectionService;

    /**
     * 執行創建訪客預約申請
     * 
     * @param command 創建訪客預約申請命令
     * @return 創建的訪客預約申請
     * @throws NotFoundException 會議室不存在
     * @throws ValidationException 驗證失敗
     * @throws IllegalStateException 時段衝突
     */
    public GuestReservationRequest execute(CreateGuestRequestCommand command) {
        // 1. 驗證輸入
        validateCommand(command);

        // 2. 驗證會議室存在
        Room room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new NotFoundException("會議室不存在: " + command.roomId()));

        // 3. 驗證會議室狀態與容量
        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new ValidationException("會議室不可用");
        }

        if (command.attendeeCount() != null && command.attendeeCount() > room.getCapacity()) {
            throw new ValidationException("參與人數超過會議室容量");
        }

        // 4. 檢測時段衝突 (包含現有預約和待審核的訪客申請)
        boolean hasConflict = conflictDetectionService.hasConflict(
                command.roomId(),
                command.requestedStartTime(),
                command.requestedEndTime(),
                null // 新建申請，無需排除
        );
        
        if (hasConflict) {
            throw new IllegalStateException("該時段已有預約");
        }

        // 5. 創建訪客預約申請
        GuestReservationRequest request = GuestReservationRequest.builder()
                .guestName(command.guestName())
                .guestEmail(command.guestEmail())
                .guestPhone(command.guestPhone())
                .guestCompany(command.guestCompany())
                .roomId(command.roomId())
                .meetingTitle(command.meetingTitle())
                .meetingPurpose(command.purpose())
                .requestedStartTime(command.requestedStartTime())
                .requestedEndTime(command.requestedEndTime())
                .status(GuestReservationRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 6. 驗證時間區間
        request.validateTimeRange();

        // 7. 儲存申請
        GuestReservationRequest saved = guestReservationRequestRepository.save(request);

        // 8. 發送通知給管理員 (由事件處理)
        // 實際應使用 Spring Events 或 RabbitMQ

        return saved;
    }

    /**
     * 驗證命令參數
     */
    private void validateCommand(CreateGuestRequestCommand command) {
        if (command.guestName() == null || command.guestName().trim().isEmpty()) {
            throw new ValidationException("訪客姓名不能為空");
        }
        if (command.guestEmail() == null || command.guestEmail().trim().isEmpty()) {
            throw new ValidationException("訪客 Email 不能為空");
        }
        if (!isValidEmail(command.guestEmail())) {
            throw new ValidationException("Email 格式無效");
        }
        if (command.roomId() == null) {
            throw new ValidationException("會議室 ID 不能為空");
        }
        if (command.meetingTitle() == null || command.meetingTitle().trim().isEmpty()) {
            throw new ValidationException("會議標題不能為空");
        }
        if (command.requestedStartTime() == null) {
            throw new ValidationException("開始時間不能為空");
        }
        if (command.requestedEndTime() == null) {
            throw new ValidationException("結束時間不能為空");
        }
        if (!command.requestedEndTime().isAfter(command.requestedStartTime())) {
            throw new ValidationException("結束時間必須晚於開始時間");
        }
        if (command.requestedStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("開始時間不能早於現在");
        }
    }

    /**
     * 驗證 Email 格式
     */
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        // 簡單的 Email 格式驗證
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * 創建訪客預約申請命令
     */
    public record CreateGuestRequestCommand(
            String guestName,
            String guestEmail,
            String guestPhone,
            String guestCompany,
            Long roomId,
            String meetingTitle,
            LocalDateTime requestedStartTime,
            LocalDateTime requestedEndTime,
            Integer attendeeCount,
            String purpose
    ) {
        public CreateGuestRequestCommand {
            // Record validation in constructor
        }
    }
}
