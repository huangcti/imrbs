package tw.huangcti.imrbs.web.mapper;

import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.web.dto.ReservationDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 預約 DTO 映射器
 * 注意:由於 Reservation 只包含 roomId/userId,需要從外部傳入 Room/User 物件
 */
@Component
public class ReservationMapper {

    /**
     * Domain Reservation → ReservationDTO
     * @param reservation 預約領域物件
     * @param room 會議室物件 (需從 Repository 查詢)
     * @param user 使用者物件 (需從 Repository 查詢)
     */
    public ReservationDTO toDTO(Reservation reservation, Room room, User user) {
        if (reservation == null) {
            return null;
        }

        return new ReservationDTO(
                reservation.getId(),
                reservation.getRoomId(),
                room != null ? room.getName() : null,
                reservation.getUserId(),
                user != null ? user.getFullName() : null,
                reservation.getMeetingTitle(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getParticipants(),
                reservation.getStatus().name(),
                reservation.getIsRecurring(),
                reservation.getRecurringRule() != null ? reservation.getRecurringRule().toString() : null,
                reservation.getCreatedAt()
        );
    }

    /**
     * 簡化版:僅使用 Reservation 數據 (Room/User 名稱將為 null)
     */
    public ReservationDTO toDTO(Reservation reservation) {
        return toDTO(reservation, null, null);
    }

    /**
     * List<Reservation> → List<ReservationDTO>
     * 注意:此方法不包含 Room/User 資訊,僅用於簡單列表
     */
    public List<ReservationDTO> toDTOList(List<Reservation> reservations) {
        if (reservations == null) {
            return List.of();
        }
        return reservations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
