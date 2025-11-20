package tw.huangcti.imrbs.web.mapper;

import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.web.dto.ReservationDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 預約 DTO 映射器
 */
@Component
public class ReservationMapper {

    /**
     * Domain Reservation → ReservationDTO
     */
    public ReservationDTO toDTO(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return ReservationDTO.builder()
                .id(reservation.getId())
                .roomId(reservation.getRoom() != null ? reservation.getRoom().getId() : null)
                .roomName(reservation.getRoom() != null ? reservation.getRoom().getName() : null)
                .userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
                .userName(reservation.getUser() != null ? reservation.getUser().getName() : null)
                .meetingTitle(reservation.getMeetingTitle())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .participants(reservation.getParticipants())
                .status(reservation.getStatus().name())
                .isRecurring(reservation.getIsRecurring())
                .recurringRule(reservation.getRecurringRule())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    /**
     * List<Reservation> → List<ReservationDTO>
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
