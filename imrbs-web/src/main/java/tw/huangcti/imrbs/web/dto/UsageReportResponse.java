package tw.huangcti.imrbs.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 使用率報告回應 DTO
 * 用於 API 回應的資料傳輸物件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private double overallUsageRate;
    private double totalUsageHours;
    private int totalReservations;
    private List<RoomUsage> roomUsages;
    private List<DailyTrendItem> dailyTrend;

    /**
     * 會議室使用統計
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomUsage {
        private Long roomId;
        private String roomName;
        private double usageRate;
        private double usageHours;
        private int reservationCount;
    }

    /**
     * 每日趨勢項目
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        private LocalDate date;
        private double usageRate;
    }
}
