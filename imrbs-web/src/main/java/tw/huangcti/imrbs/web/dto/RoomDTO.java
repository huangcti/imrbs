package tw.huangcti.imrbs.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 會議室查詢回應 DTO
 */
@Builder
public record RoomDTO(
        Long id,
        String name,
        Integer capacity,
        String location,
        String building,
        String floor,
        String status,
        List<String> equipment,
        List<String> features,
        List<String> photos,
        BookingRuleDTO bookingRule
) {
    @Builder
    public record BookingRuleDTO(
            Integer maxHoursPerReservation,
            Integer minAdvanceBookingHours,
            Integer maxAdvanceBookingDays,
            Boolean allowRecurring,
            Boolean requiresApproval
    ) {}
}
