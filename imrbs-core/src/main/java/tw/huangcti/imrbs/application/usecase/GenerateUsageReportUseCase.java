package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.service.PopularTimeSlotsService;
import tw.huangcti.imrbs.domain.service.UsageStatisticsService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * 生成使用報告 Use Case
 * 職責: 協調使用率統計服務和熱門時段分析服務，生成完整的使用報告
 * 
 * 報告類型:
 * - 日報告: 單日統計
 * - 週報告: 當週統計 (週一至週日)
 * - 月報告: 當月統計
 * - 自訂日期範圍
 */
@RequiredArgsConstructor
public class GenerateUsageReportUseCase {

    private final UsageStatisticsService usageStatisticsService;
    private final PopularTimeSlotsService popularTimeSlotsService;

    /**
     * 生成指定日期範圍的使用報告
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @param roomIds 指定會議室 ID (可選，為空則查詢全部)
     * @return 使用報告
     */
    public UsageReport generateReport(
            LocalDate startDate, 
            LocalDate endDate, 
            List<Long> roomIds) {

        // 驗證日期
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("日期範圍不能為空");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("結束日期不能早於開始日期");
        }

        // 取得會議室使用統計
        List<UsageStatisticsService.RoomUsageStats> roomStats;
        if (roomIds == null || roomIds.isEmpty()) {
            roomStats = usageStatisticsService.calculateRoomUsageStats(startDate, endDate);
        } else {
            roomStats = usageStatisticsService.calculateRoomUsageStats(roomIds, startDate, endDate);
        }

        // 取得整體使用率
        double overallUsageRate = usageStatisticsService.calculateOverallUsageRate(startDate, endDate);
        
        // 取得總使用時數
        double totalUsageHours = usageStatisticsService.calculateTotalUsageHours(startDate, endDate);
        
        // 取得總預約次數
        int totalReservations = usageStatisticsService.countTotalReservations(startDate, endDate);

        // 取得時段分析
        PopularTimeSlotsService.TimeSlotAnalysisReport timeSlotAnalysis = 
                popularTimeSlotsService.generateAnalysisReport(startDate, endDate);

        // 組裝報告
        return UsageReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .overallUsageRate(overallUsageRate)
                .totalUsageHours(totalUsageHours)
                .totalReservations(totalReservations)
                .roomStats(roomStats)
                .hourlyStats(timeSlotAnalysis.getHourlyStats())
                .dayStats(timeSlotAnalysis.getDayStats())
                .peakHours(timeSlotAnalysis.getPeakHours())
                .offPeakHours(timeSlotAnalysis.getOffPeakHours())
                .build();
    }

    /**
     * 生成今日報告
     */
    public UsageReport generateDailyReport() {
        LocalDate today = LocalDate.now();
        return generateReport(today, today, null);
    }

    /**
     * 生成指定日期的日報告
     */
    public UsageReport generateDailyReport(LocalDate date) {
        return generateReport(date, date, null);
    }

    /**
     * 生成本週報告 (週一至週日)
     */
    public UsageReport generateWeeklyReport() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return generateReport(startOfWeek, endOfWeek, null);
    }

    /**
     * 生成指定日期所在週的週報告
     */
    public UsageReport generateWeeklyReport(LocalDate date) {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return generateReport(startOfWeek, endOfWeek, null);
    }

    /**
     * 生成本月報告
     */
    public UsageReport generateMonthlyReport() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return generateReport(startOfMonth, endOfMonth, null);
    }

    /**
     * 生成指定月份的月報告
     */
    public UsageReport generateMonthlyReport(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        return generateReport(startOfMonth, endOfMonth, null);
    }

    /**
     * 生成報告摘要 (輕量版，僅包含關鍵指標)
     */
    public ReportSummary generateSummary(LocalDate startDate, LocalDate endDate) {
        double overallUsageRate = usageStatisticsService.calculateOverallUsageRate(startDate, endDate);
        double totalUsageHours = usageStatisticsService.calculateTotalUsageHours(startDate, endDate);
        int totalReservations = usageStatisticsService.countTotalReservations(startDate, endDate);
        
        List<Integer> peakHours = popularTimeSlotsService.identifyPeakHours(startDate, endDate);
        
        Map<DayOfWeek, Integer> dailyBookings = popularTimeSlotsService.analyzeDailyBookings(startDate, endDate);
        DayOfWeek busiestDay = dailyBookings.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return ReportSummary.builder()
                .startDate(startDate)
                .endDate(endDate)
                .overallUsageRate(overallUsageRate)
                .totalUsageHours(totalUsageHours)
                .totalReservations(totalReservations)
                .peakHoursCount(peakHours.size())
                .busiestDay(busiestDay)
                .build();
    }

    /**
     * 使用報告 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class UsageReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private double overallUsageRate;
        private double totalUsageHours;
        private int totalReservations;
        private List<UsageStatisticsService.RoomUsageStats> roomStats;
        private List<PopularTimeSlotsService.TimeSlotStats> hourlyStats;
        private List<PopularTimeSlotsService.DayStats> dayStats;
        private List<String> peakHours;
        private List<String> offPeakHours;
    }

    /**
     * 報告摘要 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class ReportSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private double overallUsageRate;
        private double totalUsageHours;
        private int totalReservations;
        private int peakHoursCount;
        private DayOfWeek busiestDay;
    }
}
