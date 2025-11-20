package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;
import tw.huangcti.imrbs.exception.NotFoundException;
import tw.huangcti.imrbs.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 創建預約用例
 * 職責: 編排預約創建流程，包含驗證、衝突檢測、儲存
 */
@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ConflictDetectionService conflictDetectionService;

    /**
     * 執行創建預約
     * 
     * @param command 創建預約命令
     * @return 創建的預約
     * @throws NotFoundException 會議室或使用者不存在
     * @throws ValidationException 驗證失敗
     * @throws tw.huangcti.imrbs.exception.ConflictException 時段衝突
     */
    @Transactional
    public Reservation execute(CreateReservationCommand command) {
        // 1. 驗證輸入
        validateCommand(command);

        // 2. 驗證會議室與使用者存在
        Room room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new NotFoundException("會議室不存在: " + command.roomId()));
        
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("使用者不存在: " + command.userId()));

        // 3. 驗證會議室狀態與容量
        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new ValidationException("會議室不可用");
        }

        if (command.participants() != null && !command.participants().isEmpty()) {
            int participantCount = command.participants().split(",").length + 1; // +1 for organizer
            if (participantCount > room.getCapacity()) {
                throw new ValidationException("參與者人數超過會議室容量");
            }
        }

        // 4. 檢測衝突
        conflictDetectionService.checkConflict(
                command.roomId(),
                command.startTime(),
                command.endTime(),
                null // 新建預約，無需排除
        );

        // 5. 創建預約
        Reservation reservation = Reservation.builder()
                .room(room)
                .user(user)
                .meetingTitle(command.meetingTitle())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .participants(command.participants())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .isRecurring(command.recurringRule() != null)
                .build();

        // 6. 儲存預約
        Reservation saved = reservationRepository.save(reservation);

        // 7. 發布預約確認事件 (由基礎設施層監聽並發送通知)
        // 事件發布由 Spring Events 或 RabbitMQ 處理
        // 這裡簡化處理，實際應該使用 @TransactionalEventListener
        
        return saved;
    }

    /**
     * 驗證命令參數
     */
    private void validateCommand(CreateReservationCommand command) {
        if (command.roomId() == null) {
            throw new ValidationException("會議室 ID 不能為空");
        }
        if (command.userId() == null) {
            throw new ValidationException("使用者 ID 不能為空");
        }
        if (command.meetingTitle() == null || command.meetingTitle().isBlank()) {
            throw new ValidationException("會議主題不能為空");
        }
        if (command.startTime() == null || command.endTime() == null) {
            throw new ValidationException("開始時間與結束時間不能為空");
        }
        if (command.endTime().isBefore(command.startTime())) {
            throw new ValidationException("結束時間不能早於開始時間");
        }
        if (command.startTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("預約時間不能早於當前時間");
        }
    }

    /**
     * 創建預約命令
     */
    public record CreateReservationCommand(
            Long roomId,
            Long userId,
            String meetingTitle,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String participants,
            String recurringRule
    ) {}
}
