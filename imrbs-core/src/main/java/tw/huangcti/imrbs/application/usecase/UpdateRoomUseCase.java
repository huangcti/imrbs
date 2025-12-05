package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.exception.ConflictException;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.util.Optional;

/**
 * T115 [P] [US4] 更新會議室用例 (Application Use Case - Framework Agnostic)
 * 職責: 編排會議室更新流程，包含驗證、存在檢查、重複檢查、儲存
 * 注意: 此類別為 POJO，由基礎設施層 (Infrastructure) 使用 @Service 包裝
 */
@RequiredArgsConstructor
public class UpdateRoomUseCase {

    private final RoomRepository roomRepository;

    /**
     * 執行更新會議室
     * 
     * @param roomId 要更新的會議室 ID
     * @param updatedRoom 更新後的會議室資料
     * @return 更新後的會議室
     * @throws NotFoundException 會議室不存在
     * @throws ValidationException 驗證失敗
     * @throws ConflictException 會議室名稱重複
     */
    public Room execute(Long roomId, Room updatedRoom) {
        // 1. 驗證會議室存在
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("會議室不存在: " + roomId));

        // 2. 驗證輸入
        validateRoom(updatedRoom);

        // 3. 檢查名稱是否與其他會議室重複
        if (!existingRoom.getName().equals(updatedRoom.getName())) {
            Optional<Room> duplicateRoom = roomRepository.findByName(updatedRoom.getName());
            if (duplicateRoom.isPresent() && !duplicateRoom.get().getId().equals(roomId)) {
                throw new ConflictException("會議室名稱已存在: " + updatedRoom.getName());
            }
        }

        // 4. 合併更新（保留 ID 和創建時間）
        Room roomToUpdate = existingRoom.toBuilder()
                .name(updatedRoom.getName())
                .building(updatedRoom.getBuilding())
                .floor(updatedRoom.getFloor())
                .locationDescription(updatedRoom.getLocationDescription())
                .capacity(updatedRoom.getCapacity())
                .equipment(updatedRoom.getEquipment())
                .photos(updatedRoom.getPhotos())
                .status(updatedRoom.getStatus() != null ? updatedRoom.getStatus() : existingRoom.getStatus())
                .features(updatedRoom.getFeatures())
                .bookingRule(updatedRoom.getBookingRule())
                .build();

        // 5. 儲存更新
        return roomRepository.save(roomToUpdate);
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
