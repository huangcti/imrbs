package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

/**
 * T114 [P] [US4] 創建會議室用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排會議室創建流程，包含驗證、重複檢查、儲存
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class CreateRoomUseCase {

    private final RoomRepository roomRepository;

    /**
     * 執行創建會議室
     * 
     * @param room 要創建的會議室實體
     * @return 創建的會議室
     * @throws ValidationException 驗證失敗
     * @throws ConflictException 會議室名稱重複
     */
    public Room execute(Room room) {
        // 1. 驗證輸入
        validateRoom(room);

        // 2. 檢查會議室名稱是否已存在
        if (roomRepository.findByName(room.getName()).isPresent()) {
            throw new ConflictException("會議室名稱已存在: " + room.getName());
        }

        // 3. 設定預設值
        if (room.getStatus() == null) {
            room = room.toBuilder().status(Room.RoomStatus.AVAILABLE).build();
        }

        // 4. 儲存會議室
        return roomRepository.save(room);
    }

    /**
     * 驗證會議室資料
     */
    private void validateRoom(Room room) {
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new ValidationException("會議室名稱不能為空");
        }

        if (room.getFloor() == null || room.getFloor().trim().isEmpty()) {
            throw new ValidationException("樓層不能為空");
        }

        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ValidationException("容納人數必須大於 0");
        }

        // 驗證狀態有效性
        if (room.getStatus() != null && 
            room.getStatus() != Room.RoomStatus.AVAILABLE &&
            room.getStatus() != Room.RoomStatus.MAINTENANCE &&
            room.getStatus() != Room.RoomStatus.DISABLED) {
            throw new ValidationException("無效的會議室狀態");
        }
    }
}
