package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * T116 [P] [US4] 刪除會議室用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排會議室刪除流程，包含存在檢查、預約衝突檢查、軟刪除
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class DeleteRoomUseCase {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 執行刪除會議室 (軟刪除，設為 DISABLED 狀態)
     * 
     * @param roomId 要刪除的會議室 ID
     * @throws NotFoundException 會議室不存在
     * @throws ConflictException 會議室有進行中或未來的預約
     */
    public void execute(Long roomId) {
        // 1. 驗證會議室存在
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("會議室不存在: " + roomId));

        // 2. 檢查會議室是否有進行中或未來的預約
        checkActiveReservations(roomId);

        // 3. 軟刪除: 設定狀態為 DISABLED
        Room disabledRoom = room.toBuilder()
                .status(Room.RoomStatus.DISABLED)
                .build();

        roomRepository.save(disabledRoom);
    }

    /**
     * 檢查會議室是否有進行中或未來的確認預約
     * 
     * @param roomId 會議室 ID
     * @throws ConflictException 如果有進行中或未來的預約
     */
    private void checkActiveReservations(Long roomId) {
        List<Reservation> reservations = reservationRepository.findByRoomId(roomId);
        
        LocalDateTime now = LocalDateTime.now();
        
        // 檢查是否有 CONFIRMED 狀態且結束時間在未來的預約
        boolean hasActiveReservations = reservations.stream()
                .anyMatch(reservation -> 
                    reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED &&
                    reservation.getEndTime().isAfter(now)
                );

        if (hasActiveReservations) {
            throw new ConflictException(
                "無法刪除會議室，因為存在進行中或未來的預約。請先取消所有相關預約。"
            );
        }
    }
}
