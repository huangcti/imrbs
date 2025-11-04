package tw.huangcti.imrbs.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 會議室狀態回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusResponse {
    private LocalDate date;
    private String location;
    private List<RoomStatusItem> rooms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomStatusItem {
        private String roomId;
        private String roomName;
        private String floor;
        private List<TimeSlot> timeSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String startTime;
        private String endTime;
        private String status; // AVAILABLE, RESERVED, CANCELLED
        private String reservationId;
        private String title;
    }
}
