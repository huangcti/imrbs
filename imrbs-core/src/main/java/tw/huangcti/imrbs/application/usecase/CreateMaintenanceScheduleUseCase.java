package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * T117 [P] [US4] 創建維護時段用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排維護時段創建流程，包含驗證、會議室檢查、時間衝突檢查、儲存
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class CreateMaintenanceScheduleUseCase {

    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 執行創建維護時段
     * 
     * @param maintenanceSchedule 維護時段資料
     * @return 創建後的維護時段
     * @throws NotFoundException 會議室不存在
     * @throws ValidationException 驗證失敗
     * @throws ConflictException 時間衝突
     */
    public MaintenanceSchedule execute(MaintenanceSchedule maintenanceSchedule) {
        // 1. 驗證輸入
        validateMaintenanceSchedule(maintenanceSchedule);

        // 2. 驗證會議室存在且狀態可用
        Room room = roomRepository.findById(maintenanceSchedule.getRoomId())
                .orElseThrow(() -> new NotFoundException(
                    "會議室不存在: " + maintenanceSchedule.getRoomId()
                ));

        if (room.getStatus() == Room.RoomStatus.DISABLED) {
            throw new ValidationException("會議室已停用，無法排程維護: " + room.getName());
        }

        // 3. 檢查是否與其他維護時段衝突
        checkMaintenanceConflict(
            maintenanceSchedule.getRoomId(),
            maintenanceSchedule.getStartTime(),
            maintenanceSchedule.getEndTime()
        );

        // 4. 檢查是否與現有預約衝突
        checkReservationConflict(
            maintenanceSchedule.getRoomId(),
            maintenanceSchedule.getStartTime(),
            maintenanceSchedule.getEndTime()
        );

        // 5. 儲存維護時段
        MaintenanceSchedule savedSchedule = maintenanceScheduleRepository.save(
            maintenanceSchedule.toBuilder()
                .createdAt(LocalDateTime.now())
                .build()
        );

        // 6. 更新會議室狀態為維護中 (如果維護已開始)
        if (maintenanceSchedule.getStartTime().isBefore(LocalDateTime.now()) ||
            maintenanceSchedule.getStartTime().isEqual(LocalDateTime.now())) {
            updateRoomToMaintenanceStatus(room);
        }

        return savedSchedule;
    }

    /**
     * 驗證維護時段資料
     */
    private void validateMaintenanceSchedule(MaintenanceSchedule schedule) {
        if (schedule.getRoomId() == null) {
            throw new ValidationException("會議室 ID 不能為空");
        }

        if (schedule.getStartTime() == null) {
            throw new ValidationException("開始時間不能為空");
        }

        if (schedule.getEndTime() == null) {
            throw new ValidationException("結束時間不能為空");
        }

        if (schedule.getReason() == null || schedule.getReason().trim().isEmpty()) {
            throw new ValidationException("維護原因不能為空");
        }

        if (schedule.getCreatedBy() == null) {
            throw new ValidationException("創建者 ID 不能為空");
        }

        // 驗證時間範圍
        LocalDateTime now = LocalDateTime.now();
        
        if (schedule.getStartTime().isBefore(now)) {
            throw new ValidationException("維護開始時間不能早於現在");
        }

        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new ValidationException("結束時間必須晚於開始時間");
        }

        // 驗證維護時段長度（不超過 7 天）
        if (schedule.getEndTime().isAfter(schedule.getStartTime().plusDays(7))) {
            throw new ValidationException("維護時段不能超過 7 天");
        }
    }

    /**
     * 檢查是否與其他維護時段衝突
     */
    private void checkMaintenanceConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasConflict = maintenanceScheduleRepository.hasMaintenanceInTimeRange(
            roomId, startTime, endTime
        );

        if (hasConflict) {
            throw new ConflictException(
                "維護時段與現有維護排程衝突。請選擇其他時間。"
            );
        }
    }

    /**
     * 檢查是否與現有預約衝突
     */
    private void checkReservationConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> conflictingReservations = reservationRepository.findByRoomIdAndTimeRange(
            roomId, startTime, endTime
        );

        // 過濾出狀態為 CONFIRMED 的預約
        boolean hasActiveReservations = conflictingReservations.stream()
                .anyMatch(reservation -> 
                    reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED
                );

        if (hasActiveReservations) {
            throw new ConflictException(
                "維護時段與現有預約衝突。請先取消相關預約或選擇其他時間。"
            );
        }
    }

    /**
     * 更新會議室狀態為維護中
     */
    private void updateRoomToMaintenanceStatus(Room room) {
        if (room.getStatus() != Room.RoomStatus.MAINTENANCE) {
            Room maintenanceRoom = room.toBuilder()
                    .status(Room.RoomStatus.MAINTENANCE)
                    .build();
            roomRepository.save(maintenanceRoom);
        }
    }
}
